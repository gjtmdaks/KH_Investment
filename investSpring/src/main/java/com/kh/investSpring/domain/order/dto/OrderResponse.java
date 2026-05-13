package com.kh.investSpring.domain.order.dto;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderResponse {
	
    private String stockCode;      // 종목코드
    private String orderKind;      // BUY / SELL
    private String orderType;      // MARKET / LIMIT
    private BigDecimal price;      // 주문 가격
    private Long quantity;         // 주문 수량
    private String status;         // PENDING / FILLED / CANCELED / REJECTED
    private Date createdAt;        // 주문 시간
    
}
