package com.kh.investSpring.domain.stock.dto;

import lombok.Data;

@Data
public class StockScreenerDto {

    private String stockCode;
    private String stockName;
    private String marketType;
    private String sector;

    private Long currentPrice;
    private Double changeRate;
    private Long volume;

    private Long extraValue;
}