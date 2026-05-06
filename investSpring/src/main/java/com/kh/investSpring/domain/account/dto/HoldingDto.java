package com.kh.investSpring.domain.account.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HoldingDto {

    private String stockCode;
    private String stockName;
    private Long quantity;
    private Long avgPrice;
    private Long currentPrice;
}