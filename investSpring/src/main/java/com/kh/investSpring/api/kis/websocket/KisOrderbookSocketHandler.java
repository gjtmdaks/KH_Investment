package com.kh.investSpring.api.kis.websocket;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.kh.investSpring.api.kis.service.KisOrderbookDemandSubscriptionService;
import com.kh.investSpring.api.kis.service.KisOrderbookRealtimeCache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class KisOrderbookSocketHandler extends TextWebSocketHandler {

    private final String approvalKey;
    private final KisOrderbookRealtimeCache orderbookRealtimeCache;
    private final KisOrderbookDemandSubscriptionService demandSubscriptionService;
    private final Runnable reconnectCallback;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("KIS 호가 websocket 연결 성공 sessionId={}", session.getId());
        demandSubscriptionService.onSessionConnected(session, approvalKey);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();

            if (payload.contains("\"tr_id\":\"PINGPONG\"")) {
                session.sendMessage(new PongMessage());
                return;
            }

            if (payload.contains("SUBSCRIBE SUCCESS")) {
                return;
            }

            if (payload.contains("MAX SUBSCRIBE OVER")) {
                log.error("호가 실시간 구독 제한 초과={}", payload);
                return;
            }

            KisOrderbookPayloadMapper.parse(payload)
                    .ifPresent(orderbookRealtimeCache::put);

        } catch (Exception e) {
            log.error("호가 실시간 데이터 파싱 실패", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.warn("KIS 호가 websocket 연결 종료 sessionId={}, status={}",
                session.getId(),
                status);
        demandSubscriptionService.onSessionClosed(session);

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                reconnectCallback.run();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("KIS 호가 websocket transport error sessionId={}",
                session.getId(),
                exception);

        try {
            session.close();
        } catch (Exception ignored) {
        }
    }
}
