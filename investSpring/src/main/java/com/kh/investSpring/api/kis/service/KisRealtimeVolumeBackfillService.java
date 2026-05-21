package com.kh.investSpring.api.kis.service;

import java.util.Collection;

import org.springframework.stereotype.Service;

import com.kh.investSpring.api.kis.config.KisProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KisRealtimeVolumeBackfillService {

    private final KisProperties kisProperties;
    private final KisSubscriptionPoolService subscriptionPoolService;
    private final KisStockService kisStockService;

    public void backfillSubscribedCodes() {
        if (!kisProperties.isWebsocketEnabled()) {
            return;
        }

        Collection<String> codes = subscriptionPoolService.getSubscribedCodes();
        if (codes.isEmpty()) {
            return;
        }

        int updated = 0;
        for (String stockCode : codes) {
            try {
                kisStockService.backfillVolumeAndTradingValue(stockCode);
                updated++;
            } catch (Exception e) {
                log.debug("거래량·거래대금 REST backfill 실패 stockCode={}", stockCode, e);
            }
        }

        log.info("거래량·거래대금 REST backfill 완료 count={}", updated);
    }

    public void backfillCodes(Collection<String> stockCodes) {
        if (stockCodes == null || stockCodes.isEmpty()) {
            return;
        }

        for (String stockCode : stockCodes) {
            try {
                kisStockService.backfillVolumeAndTradingValue(stockCode);
            } catch (Exception e) {
                log.debug("거래량·거래대금 REST backfill 실패 stockCode={}", stockCode, e);
            }
        }
    }
}
