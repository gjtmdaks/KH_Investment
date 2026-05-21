package com.kh.investSpring.api.kis.schedule;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kh.investSpring.api.kis.config.KisProperties;
import com.kh.investSpring.api.kis.dao.StockTradingSnapshotDao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradingSnapshotScheduler {

    private static final int SNAPSHOT_RETENTION_DAYS = 2;

    private final KisProperties kisProperties;
    private final StockTradingSnapshotDao stockTradingSnapshotDao;

    @EventListener(ApplicationReadyEvent.class)
    public void captureOnStartup() {
        captureTradingValueSnapshots();
    }

    @Scheduled(
            fixedRateString = "${kis.main.trading-snapshot-interval-ms:300000}",
            initialDelayString = "${kis.main.trading-snapshot-initial-delay-ms:300000}"
    )
    public void captureTradingValueSnapshots() {
        try {
            int inserted = stockTradingSnapshotDao.insertSnapshotsFromCurrent();
            if (inserted <= 0) {
                log.warn(
                        "거래대금 스냅샷 스킵 — stock_realtime_current에 저장 가능한 시세 없음 "
                                + "(trading_value 또는 price×volume 필요)");
                return;
            }

            log.info(
                    "거래대금 스냅샷 저장 완료 count={}, windowMinutes={}",
                    inserted,
                    kisProperties.getMainTradingWindowMinutes());
        } catch (Exception e) {
            log.error("거래대금 스냅샷 저장 실패", e);
        }
    }

    @Scheduled(cron = "0 30 9 * * *")
    public void purgeOldTradingSnapshots() {
        try {
            int deleted = stockTradingSnapshotDao.deleteSnapshotsOlderThanDays(SNAPSHOT_RETENTION_DAYS);
            if (deleted > 0) {
                log.info("거래대금 스냅샷 정리 완료 deleted={}", deleted);
            }
        } catch (Exception e) {
            log.error("거래대금 스냅샷 정리 실패", e);
        }
    }
}
