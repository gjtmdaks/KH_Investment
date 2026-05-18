package com.kh.investSpring.api.kis.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.kh.investSpring.api.kis.dao.StockHistoryDao;
import com.kh.investSpring.api.kis.dto.KisStockCandleItemResponse;
import com.kh.investSpring.api.kis.dto.KisStockCandleResponse;
import com.kh.investSpring.api.kis.dto.StockHistoryCacheDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockHistoryReadService {

    private static final Set<String> ALLOWED_PERIODS = Set.of("D", "W", "M");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final long SYNC_COOLDOWN_MS = 60_000L;
    private static final int COVERAGE_GRACE_DAYS = 7;
    private static final int DAILY_TRAILING_GRACE_DAYS = 3;
    private static final ConcurrentHashMap<String, Long> LAST_SYNC_ATTEMPT_AT = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Object> SYNC_LOCKS = new ConcurrentHashMap<>();

    private final StockHistoryDao stockHistoryDao;
    private final KisHistoryService kisHistoryService;

    public KisStockCandleResponse getCandles(
            String stockCode,
            String period,
            LocalDate from,
            LocalDate to
    ) {
        String normalizedPeriod = normalizePeriod(period);

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from은 to보다 이후일 수 없습니다.");
        }

        List<StockHistoryCacheDto> rows = stockHistoryDao.selectHistoryByRange(
                stockCode,
                normalizedPeriod,
                from,
                to
        );

        if (!kisHistoryService.isBulkSyncInProgress()) {
            if (needsHistorySync(rows, from, to, normalizedPeriod)) {
                try {
                    maybeSyncOnce(stockCode, normalizedPeriod);
                } catch (Exception e) {
                    log.warn("과거 시세 보완 동기화 실패 stockCode={} period={}",
                            stockCode,
                            normalizedPeriod,
                            e
                    );
                }
            } else if (needsHistoryRepair(rows)) {
                log.info(
                        "과거 시세 종가 누락 캐시 복구 재적재 stockCode={} period={} bars={}",
                        stockCode,
                        normalizedPeriod,
                        rows.size()
                );

                try {
                    forceReSync(stockCode, normalizedPeriod);
                } catch (Exception e) {
                    log.warn("과거 시세 재적재 실패 stockCode={} period={}",
                            stockCode,
                            normalizedPeriod,
                            e
                    );
                }
            }

            rows = stockHistoryDao.selectHistoryByRange(
                    stockCode,
                    normalizedPeriod,
                    from,
                    to
            );
        } else {
            log.warn("전체 동기화 실행 중이라 캔들 보완 동기화를 건너뜀 stockCode={} period={}",
                    stockCode,
                    normalizedPeriod
            );
        }

        List<KisStockCandleItemResponse> candles = rows.stream()
                .filter(row -> row.getBaseDate() != null)
                .map(this::toCandleItem)
                .toList();

        log.debug("캔들 조회 완료 stockCode={} period={} from={} to={} count={}",
                stockCode,
                normalizedPeriod,
                from,
                to,
                candles.size()
        );

        return new KisStockCandleResponse(
                stockCode,
                normalizedPeriod,
                DATE_FORMAT.format(from),
                DATE_FORMAT.format(to),
                candles
        );
    }

    private boolean needsHistorySync(
            List<StockHistoryCacheDto> rows,
            LocalDate from,
            LocalDate to,
            String period
    ) {
        if (rows.isEmpty()) {
            return true;
        }

        LocalDate minDate = rows.stream()
                .map(StockHistoryCacheDto::getBaseDate)
                .filter(date -> date != null)
                .min(Comparator.naturalOrder())
                .orElse(null);
        LocalDate maxDate = rows.stream()
                .map(StockHistoryCacheDto::getBaseDate)
                .filter(date -> date != null)
                .max(Comparator.naturalOrder())
                .orElse(null);

        if (minDate == null || maxDate == null) {
            return true;
        }

        LocalDate leadingThreshold = from.plusDays(COVERAGE_GRACE_DAYS);

        if (minDate.isAfter(leadingThreshold)) {
            return true;
        }

        int trailingGraceDays = "D".equals(period) ? DAILY_TRAILING_GRACE_DAYS : COVERAGE_GRACE_DAYS;
        LocalDate trailingThreshold = to.minusDays(trailingGraceDays);

        return maxDate.isBefore(trailingThreshold);
    }

    private boolean needsHistoryRepair(List<StockHistoryCacheDto> rows) {
        return rows.stream().anyMatch(row -> {
            long open = safeLong(row.getOpenPrice());
            long close = safeLong(row.getClosePrice());

            return open > 0L && close <= 0L;
        });
    }

    private void forceReSync(String stockCode, String period) throws Exception {
        String key = stockCode + ":" + period;
        LAST_SYNC_ATTEMPT_AT.remove(key);
        maybeSyncOnce(stockCode, period);
    }

    private void maybeSyncOnce(String stockCode, String period) throws Exception {
        String key = stockCode + ":" + period;
        long now = System.currentTimeMillis();

        Long lastAttemptAt = LAST_SYNC_ATTEMPT_AT.get(key);
        if (lastAttemptAt != null && now - lastAttemptAt < SYNC_COOLDOWN_MS) {
            log.debug("최근 보완 동기화 시도가 있어 건너뜀 stockCode={} period={}", stockCode, period);
            return;
        }

        Object lock = SYNC_LOCKS.computeIfAbsent(key, ignored -> new Object());
        synchronized (lock) {
            long now2 = System.currentTimeMillis();
            Long lastAttemptAt2 = LAST_SYNC_ATTEMPT_AT.get(key);
            if (lastAttemptAt2 != null && now2 - lastAttemptAt2 < SYNC_COOLDOWN_MS) {
                return;
            }

            boolean saved = kisHistoryService.syncHistory(stockCode, period);

            if (saved) {
                LAST_SYNC_ATTEMPT_AT.put(key, now2);
            }
        }
    }

    private String normalizePeriod(String period) {
        if (period == null || period.isBlank()) {
            throw new IllegalArgumentException("period는 필수입니다.");
        }

        String normalized = period.trim().toUpperCase();

        if (!ALLOWED_PERIODS.contains(normalized)) {
            throw new IllegalArgumentException("period는 D, W, M 중 하나여야 합니다.");
        }

        return normalized;
    }

    private KisStockCandleItemResponse toCandleItem(StockHistoryCacheDto row) {
        if (row.getBaseDate() == null) {
            log.warn("캔들 기준일이 없어 제외 stockCode={} period={}",
                    row.getStockCode(),
                    row.getPeriodType()
            );
            throw new IllegalStateException("캔들 기준일이 없습니다.");
        }

        return new KisStockCandleItemResponse(
                DATE_FORMAT.format(row.getBaseDate()),
                safeLong(row.getOpenPrice()),
                safeLong(row.getHighPrice()),
                safeLong(row.getLowPrice()),
                safeLong(row.getClosePrice()),
                safeLong(row.getVolume())
        );
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }
}
