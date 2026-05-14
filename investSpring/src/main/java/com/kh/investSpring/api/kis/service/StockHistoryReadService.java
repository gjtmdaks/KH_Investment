package com.kh.investSpring.api.kis.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private static final long SYNC_COOLDOWN_MS = 10 * 60 * 1000L;
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

        if (rows.isEmpty()) {
            if (!kisHistoryService.isBulkSyncInProgress()) {
                try {
                    maybeSyncOnce(stockCode, normalizedPeriod);
                } catch (Exception e) {
                    log.warn("과거 시세 보완 동기화 실패 stockCode={} period={}",
                            stockCode,
                            normalizedPeriod,
                            e
                    );
                }
            } else {
                log.warn("전체 동기화 실행 중이라 캔들 보완 동기화를 건너뜀 stockCode={} period={}",
                        stockCode,
                        normalizedPeriod
                );
            }

            rows = stockHistoryDao.selectHistoryByRange(
                    stockCode,
                    normalizedPeriod,
                    from,
                    to
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

            LAST_SYNC_ATTEMPT_AT.put(key, now2);
            kisHistoryService.syncHistory(stockCode, period);
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
