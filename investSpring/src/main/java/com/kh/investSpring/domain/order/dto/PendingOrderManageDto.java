package com.kh.investSpring.domain.order.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PendingOrderManageDto {

    private Long orderId;
    private Long userNo;
    private Long accountNo;

    private String orderKind;
    private String orderType;
    private String stockCode;
    private String status;

    private BigDecimal price;
    private Long quantity;
}