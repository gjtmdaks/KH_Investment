package com.kh.investSpring.domain.order.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PendingOrderDto {

    private Long orderId;
    private Long userNo;
    private Long accountNo;

    private String stockCode;
    private String orderKind;
    private String orderType;

    private BigDecimal orderPrice;   // 예약 주문 가격
    private Long quantity;

    private BigDecimal currentPrice; // 현재가
}