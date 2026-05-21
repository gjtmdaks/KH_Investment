package com.kh.investSpring.api.kis.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kh.investSpring.api.kis.config.KisProperties;
import com.kh.investSpring.api.kis.service.KisRealtimeVolumeBackfillService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RealtimeVolumeBackfillScheduler {

    private final KisProperties kisProperties;
    private final KisRealtimeVolumeBackfillService volumeBackfillService;

    @Scheduled(fixedDelayString = "${kis.realtime.volume-backfill-interval-ms:30000}")
    public void backfillSubscribedVolumeAndTradingValue() {
        if (!kisProperties.isWebsocketEnabled()) {
            return;
        }

        volumeBackfillService.backfillSubscribedCodes();
    }
}
