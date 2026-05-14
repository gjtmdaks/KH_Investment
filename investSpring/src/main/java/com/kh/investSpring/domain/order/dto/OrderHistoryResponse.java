package com.kh.investSpring.domain.order.dto;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderHistoryResponse {

    private Long orderId;        // 주문번호
    private String orderKind;    // BUY / SELL
    private String orderType;    // MARKET / LIMIT
    private String stockCode;    // 종목코드
    private String stockName;    // 종목명
    private BigDecimal price;    // 주문가격
    private Long quantity;       // 주문수량
    private String status;       // PENDING / FILLED / CANCELED / REJECTED
    private Date createdAt;      // 주문시간
}
