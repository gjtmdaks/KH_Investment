package com.kh.investSpring.api.kis.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kh.investSpring.api.kis.config.KisProperties;
import com.kh.investSpring.api.kis.service.KisSubscriptionPoolService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisSubscriptionRebalanceScheduler {

    private final KisProperties kisProperties;
    private final KisSubscriptionPoolService subscriptionPoolService;

    @Scheduled(
            fixedDelayString = "${kis.websocket.rebalance-delay-ms:180000}",
            initialDelayString = "${kis.websocket.rebalance-initial-delay-ms:120000}"
    )
    public void rebalanceWebsocketSubscriptions() {
        if (!kisProperties.isWebsocketEnabled()) {
            return;
        }

        try {
            subscriptionPoolService.rebalance();
        } catch (Exception e) {
            log.error("KIS WS 구독 재배치 실패", e);
        }
    }
}
