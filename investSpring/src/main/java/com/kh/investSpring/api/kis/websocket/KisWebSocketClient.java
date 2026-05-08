package com.kh.investSpring.api.kis.websocket;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.investSpring.api.kis.config.KisProperties;
import com.kh.investSpring.api.kis.dto.KisRealtimeRequest;
import com.kh.investSpring.api.kis.dto.StockRealtimeTickDto;
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

    @EventListener(ApplicationReadyEvent.class)
    public void connect() {

        if (!properties.isWebsocketEnabled()) {
            log.info("KIS websocket 비활성화됨(kis.websocket.enabled=false). 실시간 연결을 건너뜁니다.");
            return;
        }

        try {

            String approvalKey =
                    approvalService.getApprovalKey();

            StandardWebSocketClient client =
                    new StandardWebSocketClient();

            client.doHandshake(
                    new KisSocketHandler(
                            approvalKey,
                            queueService,
                            objectMapper,
                            stockDao
                    ),
                    properties.getWebsocketUrl()
                            + "/tryitout/H0STCNT0"
            );

        } catch (Exception e) {
            log.error("KIS websocket 연결 실패", e);
        }
    }

    @RequiredArgsConstructor
    static class KisSocketHandler extends TextWebSocketHandler {

        private final String approvalKey;
        private final RealtimeQueueService queueService;
        private final ObjectMapper objectMapper;
        private final StockDao stockDao;

        @Override
        public void afterConnectionEstablished(
                WebSocketSession session
        ) throws Exception {

            log.info("KIS websocket 연결 성공");

            List<String> stockCodes = stockDao.findAllStockCodes();

            log.info("구독 대상 종목 수={}", stockCodes.size());

            for (String stockCode : stockCodes) {
                try {

                    subscribe(session, stockCode);
                    Thread.sleep(50);

                } catch (Exception e) {
                    log.error("구독 실패 stockCode={}", stockCode, e);
                }
            }
        }

        @Override
        protected void handleTextMessage(
                WebSocketSession session,
                TextMessage message
        ) {

            try {

                String payload = message.getPayload();

                log.info(payload);

                // pingpong 처리
                if (payload.contains("\"tr_id\":\"PINGPONG\"")) {

                    session.sendMessage(new PongMessage());
                    log.info("PINGPONG 응답 전송");
                    return;
                }

                if (payload.contains("SUBSCRIBE SUCCESS")) {
                    log.info("구독 성공 응답={}", payload);
                    return;
                }

                if (payload.contains("MAX SUBSCRIBE OVER")) {
                    log.error("실시간 구독 제한 초과={}", payload);
                    return;
                }

                parseRealtime(payload);

            } catch (Exception e) {
                log.error("실시간 데이터 파싱 실패", e);
            }
        }

        private void parseRealtime(String payload) {

            if (!payload.startsWith("0|H0STCNT0")) {
                return;
            }

            String[] split = payload.split("\\|");

            if (split.length < 4) {
                return;
            }

            String body = split[3];

            String[] data = body.split("\\^");

            /*
             * KIS 실시간 체결가 index
             *
             * [0] 종목코드
             * [1] 체결시간
             * [2] 현재가
             * [5] 등락률
             * [12] 거래량
             */
            if (data.length < 13) {
                return;
            }

            String stockCode = data[0];
            log.info("실시간 종목코드={}", stockCode);
            
            String time = data[1];
            long currentPrice = Long.parseLong(data[2]);
            double changeRate = Double.parseDouble(data[5]);
            long volume = Long.parseLong(data[12]);

            LocalDate today = LocalDate.now();
            LocalTime localTime = LocalTime.parse(
				                        time,
				                        java.time.format.DateTimeFormatter.ofPattern("HHmmss")
				                    );

            LocalDateTime tradeTime = LocalDateTime.of(today, localTime);

            StockRealtimeTickDto dto = StockRealtimeTickDto.builder()
							                            .stockCode(stockCode)
							                            .currentPrice(currentPrice)
							                            .changeRate(changeRate)
							                            .volume(volume)
							                            .tradeTime(tradeTime)
							                            .build();
            queueService.add(dto);

            log.info("QUEUE 적재 완료 stockCode={}, price={}, volume={}",
	                stockCode,
	                currentPrice,
	                volume
            );
        }

        private void subscribe(
                WebSocketSession session,
                String stockCode
        ) throws Exception {

            KisRealtimeRequest request =
                    new KisRealtimeRequest(
                            new KisRealtimeRequest.Header(
                                    approvalKey,
                                    "P",
                                    "1",
                                    "utf-8"
                            ),
                            new KisRealtimeRequest.Body(
                                    new KisRealtimeRequest.Input(
                                            "H0STCNT0",
                                            stockCode
                                    )
                            )
                    );

            String json =
                    objectMapper.writeValueAsString(request);

            session.sendMessage(
                    new TextMessage(json)
            );

            log.info("KIS 구독 완료: {}", stockCode);
        }
    }
}