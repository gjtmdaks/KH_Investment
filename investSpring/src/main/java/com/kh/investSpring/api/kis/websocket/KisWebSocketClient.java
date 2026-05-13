package com.kh.investSpring.api.kis.websocket;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.investSpring.api.kis.config.KisProperties;
import com.kh.investSpring.api.kis.service.KisApprovalService;
import com.kh.investSpring.api.kis.service.RealtimeQueueService;
import com.kh.investSpring.domain.stock.dao.StockDao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisWebSocketClient {

    private final KisApprovalService approvalService;
    private final KisProperties properties;
    private final RealtimeQueueService queueService;
    private final ObjectMapper objectMapper;
    private final StockDao stockDao;
    private final StandardWebSocketClient StandardWebSocketClient;
    private final AtomicBoolean connecting = new AtomicBoolean(false);

    @EventListener(ApplicationReadyEvent.class)
    public void connect() {
        if (!properties.isWebsocketEnabled()) {
            log.info("KIS websocket 비활성화됨(kis.websocket.enabled=false). 실시간 연결을 건너뜁니다.");
            return;
        }
        if (!connecting.compareAndSet(false, true)) {
            log.warn("이미 websocket reconnect 진행 중");
            return;
        }

        try {
            String approvalKey = approvalService.getApprovalKey();
            String url = properties.getWebsocketUrl() + "/tryitout/H0STCNT0";

            StandardWebSocketClient.doHandshake(
            	    new KisSocketHandler(
            	        approvalKey,
            	        queueService,
            	        objectMapper,
            	        stockDao,
            	        this::connect
            	    ),
            	    url
            	);

        } catch (Exception e) {
            log.error("KIS websocket 연결 실패", e);
        } finally {
            connecting.set(false);
        }
    }
}