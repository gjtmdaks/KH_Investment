package com.kh.investSpring.api.kis.websocket;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import com.kh.investSpring.api.kis.config.KisProperties;
import com.kh.investSpring.api.kis.service.KisApprovalService;
import com.kh.investSpring.api.kis.service.KisOrderbookDemandSubscriptionService;
import com.kh.investSpring.api.kis.service.KisOrderbookRealtimeCache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisOrderbookWebSocketClient {

    private final KisApprovalService approvalService;
    private final KisProperties properties;
    private final KisOrderbookRealtimeCache orderbookRealtimeCache;
    private final KisOrderbookDemandSubscriptionService demandSubscriptionService;
    private final StandardWebSocketClient standardWebSocketClient;
    private final AtomicBoolean connecting = new AtomicBoolean(false);

    @EventListener(ApplicationReadyEvent.class)
    public void connect() {
        if (!properties.isWebsocketEnabled()) {
            log.info("KIS 호가 websocket 비활성화됨(kis.websocket.enabled=false). 호가 실시간 연결을 건너뜁니다.");
            return;
        }

        if (!connecting.compareAndSet(false, true)) {
            log.warn("이미 호가 websocket reconnect 진행 중");
            return;
        }

        try {
            String approvalKey = approvalService.getApprovalKey();
            String url = properties.getWebsocketUrl() + "/tryitout/" + properties.getWebsocketOrderbookTrId();

            standardWebSocketClient.doHandshake(
                    new KisOrderbookSocketHandler(
                            approvalKey,
                            orderbookRealtimeCache,
                            demandSubscriptionService,
                            this::connect
                    ),
                    url
            );

        } catch (Exception e) {
            log.error("KIS 호가 websocket 연결 실패", e);
        } finally {
            connecting.set(false);
        }
    }
}
