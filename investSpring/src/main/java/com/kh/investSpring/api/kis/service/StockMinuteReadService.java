package com.kh.investSpring.api.kis.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.kh.investSpring.api.kis.dao.StockIntradayMinuteDao;
import com.kh.investSpring.api.kis.dto.KisStockCandleItemResponse;
import com.kh.investSpring.api.kis.dto.KisStockCandleResponse;
import com.kh.investSpring.api.kis.dto.StockIntradayMinuteCacheDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockMinuteReadService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final Set<Integer> ALLOWED_INTERVALS = Set.of(1, 15, 30, 60);
    private static final int SESSION_START_MINUTES = 9 * 60;
    private static final DateTimeFormatter ISO_KST = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final long INGEST_COOLDOWN_MS = 60_000L;

    private final StockIntradayMinuteDao stockIntradayMinuteDao;
    private final KisIntradayMinuteIngestService kisIntradayMinuteIngestService;
    private final KisHistoryService kisHistoryService;

    private final ConcurrentHashMap<String, Long> lastIngestAt = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> ingestLocks = new ConcurrentHashMap<>();

    public KisStockCandleResponse getMinuteCandles(
            String stockCode,
            int intervalMinutes,
            LocalDate tradeDate
    ) {
        if (!ALLOWED_INTERVALS.contains(intervalMinutes)) {
            throw new IllegalArgumentException("intervalMinutes는 1, 15, 30, 60 중 하나여야 합니다.");
        }

        LocalDate today = LocalDate.now(SEOUL);
        LocalDate resolvedDate = tradeDate != null ? tradeDate : today;

        List<StockIntradayMinuteCacheDto> rows =
                stockIntradayMinuteDao.selectByStockAndDate(stockCode, resolvedDate);

        if (rows.isEmpty() && resolvedDate.equals(today) && !kisHistoryService.isBulkSyncInProgress()) {
            maybeIngest(stockCode, resolvedDate);

            rows = stockIntradayMinuteDao.selectByStockAndDate(stockCode, resolvedDate);
        }

        List<KisStockCandleItemResponse> candles = aggregate(rows, intervalMinutes, resolvedDate);

        String fromLabel = resolvedDate.toString();
        String toLabel = resolvedDate.toString();

        log.debug(
                "분봉 조회 완료 stockCode={} interval={} tradeDate={} count={}",
                stockCode,
                intervalMinutes,
                resolvedDate,
                candles.size()
        );

        return new KisStockCandleResponse(
                stockCode,
                "M" + intervalMinutes,
                fromLabel,
                toLabel,
                candles
        );
    }

    private void maybeIngest(String stockCode, LocalDate tradeDate) {
        String key = stockCode + ":" + tradeDate;
        long now = System.currentTimeMillis();
        Long last = lastIngestAt.get(key);

        if (last != null && now - last < INGEST_COOLDOWN_MS) {
            log.debug("분봉 적재 쿨다운 stockCode={} tradeDate={}", stockCode, tradeDate);

            return;
        }

        Object lock = ingestLocks.computeIfAbsent(key, ignored -> new Object());

        synchronized (lock) {
            long now2 = System.currentTimeMillis();
            Long last2 = lastIngestAt.get(key);

            if (last2 != null && now2 - last2 < INGEST_COOLDOWN_MS) {
                return;
            }

            lastIngestAt.put(key, now2);

            try {
                kisIntradayMinuteIngestService.ingestTradingDay(stockCode, tradeDate);
            } catch (Exception e) {
                log.warn("분봉 적재 실패 stockCode={} tradeDate={}", stockCode, tradeDate, e);
            }
        }
    }

    private List<KisStockCandleItemResponse> aggregate(
            List<StockIntradayMinuteCacheDto> oneMinuteRows,
            int intervalMinutes,
            LocalDate tradeDate
    ) {
        List<StockIntradayMinuteCacheDto> sorted = new ArrayList<>(oneMinuteRows);
        sorted.sort(Comparator.comparing(StockIntradayMinuteCacheDto::getBarTime));

        if (intervalMinutes == 1) {
            return sorted.stream()
                    .map(row -> toCandleItem(row, tradeDate))
                    .toList();
        }

        TreeMap<Integer, List<StockIntradayMinuteCacheDto>> buckets = new TreeMap<>();

        for (StockIntradayMinuteCacheDto row : sorted) {
            int bucketKey = bucketIndex(row.getBarTime(), intervalMinutes);

            buckets.computeIfAbsent(bucketKey, ignored -> new ArrayList<>()).add(row);
        }

        List<KisStockCandleItemResponse> out = new ArrayList<>();

        for (Map.Entry<Integer, List<StockIntradayMinuteCacheDto>> e : buckets.entrySet()) {
            List<StockIntradayMinuteCacheDto> chunk = e.getValue();

            if (chunk.isEmpty()) {
                continue;
            }

            long open = safeLong(chunk.get(0).getOpenPrice());
            long close = safeLong(chunk.get(chunk.size() - 1).getClosePrice());
            long high = chunk.stream()
                    .mapToLong(r -> safeLong(r.getHighPrice()))
                    .max()
                    .orElse(0L);
            long low = chunk.stream()
                    .mapToLong(r -> safeLong(r.getLowPrice()))
                    .min()
                    .orElse(0L);
            long vol = chunk.stream()
                    .mapToLong(r -> safeLong(r.getVolume()))
                    .sum();

            LocalTime bucketStart = LocalTime.MIN.plusMinutes(SESSION_START_MINUTES + (long) e.getKey() * intervalMinutes);
            String dateIso =
                    LocalDateTime.of(tradeDate, bucketStart)
                            .atZone(SEOUL)
                            .format(ISO_KST);

            out.add(
                    new KisStockCandleItemResponse(
                            dateIso,
                            open,
                            high,
                            low,
                            close,
                            vol
                    )
            );
        }

        return out;
    }

    private int bucketIndex(String barTimeHhmmss, int intervalMinutes) {
        int hod = Integer.parseInt(barTimeHhmmss.substring(0, 2));
        int minute = Integer.parseInt(barTimeHhmmss.substring(2, 4));
        int minuteOfDay = hod * 60 + minute;
        int rel = minuteOfDay - SESSION_START_MINUTES;

        if (rel < 0) {
            rel = 0;
        }

        return rel / intervalMinutes;
    }

    private KisStockCandleItemResponse toCandleItem(StockIntradayMinuteCacheDto row, LocalDate tradeDate) {
        LocalTime t = parseHhmmss(row.getBarTime());

        String dateIso =
                LocalDateTime.of(tradeDate, t)
                        .atZone(SEOUL)
                        .format(ISO_KST);

        return new KisStockCandleItemResponse(
                dateIso,
                safeLong(row.getOpenPrice()),
                safeLong(row.getHighPrice()),
                safeLong(row.getLowPrice()),
                safeLong(row.getClosePrice()),
                safeLong(row.getVolume())
        );
    }

    private static LocalTime parseHhmmss(String barTime) {
        int h = Integer.parseInt(barTime.substring(0, 2));
        int m = Integer.parseInt(barTime.substring(2, 4));
        int s = Integer.parseInt(barTime.substring(4, 6));

        return LocalTime.of(h, m, s);
    }

    private static long safeLong(Long value) {
        return value == null ? 0L : value;
    }
}
