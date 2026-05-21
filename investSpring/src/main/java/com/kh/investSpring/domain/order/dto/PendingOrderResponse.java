package com.kh.investSpring.domain.order.dto;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PendingOrderResponse {

    private Long orderId;

    private String orderKind;

    private String orderType;

    private String stockCode;

    private String stockName;

    private BigDecimal price;

    private Long quantity;

    private String status;

    private Date createdAt;
}