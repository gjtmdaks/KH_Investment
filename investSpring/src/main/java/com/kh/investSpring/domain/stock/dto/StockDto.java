package com.kh.investSpring.domain.stock.dto;

import lombok.Data;

@Data
public class StockDto {
    private String stockCode;
    private String stockName;
    private Long price;
    private Double changeRate;
    private Long volume;
    private Long tradingValue;
    private String aiSentiment;
    private String aiSummary;
}