package com.kh.investSpring.domain.stock.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StockDto {
    private String stockCode;
    private String stockName;
    private Long price;
    private Double changeRate;
    private Long volume;
    private Long tradingValue;
}