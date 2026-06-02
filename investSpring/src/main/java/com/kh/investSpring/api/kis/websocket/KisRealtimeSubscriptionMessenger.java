package com.kh.investSpring.api.kis.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.investSpring.api.kis.config.KisProperties;
import com.kh.investSpring.api.kis.dto.KisRealtimeRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisRealtimeSubscriptionMessenger {

    private final ObjectMapper objectMapper;
    private final KisProperties kisProperties;

    public void subscribe(WebSocketSession session, String approvalKey, String stockCode) throws Exception {
        send(session, approvalKey, stockCode, "1");
        log.debug("KIS 구독 요청: {}", stockCode);
    }

    public void unsubscribe(WebSocketSession session, String approvalKey, String stockCode) throws Exception {
        send(session, approvalKey, stockCode, "2");
        log.debug("KIS 구독 해제 요청: {}", stockCode);
    }

    private void send(
            WebSocketSession session,
            String approvalKey,
            String stockCode,
            String trType
    ) throws Exception {
        if (session == null || !session.isOpen()) {
            throw new IllegalStateException("KIS websocket 세션이 열려 있지 않습니다.");
        }

        KisRealtimeRequest request = new KisRealtimeRequest(
                new KisRealtimeRequest.Header(
                        approvalKey,
                        "P",
                        trType,
                        "utf-8"
                ),
                new KisRealtimeRequest.Body(
                        new KisRealtimeRequest.Input(kisProperties.getWebsocketTradeTrId(), stockCode)
                )
        );

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(request)));
    }
}
