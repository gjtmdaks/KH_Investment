package com.kh.investSpring.api.kis.websocket;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.investSpring.api.kis.dto.KisRealtimeRequest;
import com.kh.investSpring.api.kis.dto.StockRealtimeTickDto;
import com.kh.investSpring.api.kis.service.RealtimeQueueService;
import com.kh.investSpring.domain.stock.dao.StockDao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class KisSocketHandler extends TextWebSocketHandler {
    private final String approvalKey;
    private final RealtimeQueueService queueService;
    private final ObjectMapper objectMapper;
    private final StockDao stockDao;
    private final Runnable reconnectCallback;
    private static final int FIELD_COUNT = 46;

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

            // pingpong 처리
            if (payload.contains("\"tr_id\":\"PINGPONG\"")) {

                session.sendMessage(new PongMessage());
                log.info("PINGPONG 응답 전송");
                return;
            }

            if (payload.contains("SUBSCRIBE SUCCESS")) {
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
    
    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status
    ) {
        log.warn("KIS websocket 연결 종료 sessionId={}, status={}",
            session.getId(),
            status
        );

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                reconnectCallback.run();
            } catch (InterruptedException ignored) {
            }
        }).start();
    }

    @Override
    public void handleTransportError(
            WebSocketSession session,
            Throwable exception
    ) {
        log.error("KIS websocket transport error sessionId={}",
            session.getId(),
            exception
        );
        
        try {
            session.close();
        } catch (Exception ignored) {
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

        int count = Integer.parseInt(split[2]);

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
        for (int i = 0; i < count; i++) {

            int start = i * FIELD_COUNT;

            if (data.length < start + FIELD_COUNT) {
                break;
            }

            String stockCode = data[start];
            String time = data[start + 1];

            long currentPrice = parseLongSafe(data[start + 2]);
            double changeRate = parseDoubleSafe(data[start + 5]);
            long volume = parseLongSafe(data[start + 12]);

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
        }
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

        String json = objectMapper.writeValueAsString(request);

        session.sendMessage(new TextMessage(json));

        log.debug("KIS 구독 완료: {}", stockCode);
    }
    
    private long parseLongSafe(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return 0L;
        }
    }
    
    private Double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0D;
        }
    }
}