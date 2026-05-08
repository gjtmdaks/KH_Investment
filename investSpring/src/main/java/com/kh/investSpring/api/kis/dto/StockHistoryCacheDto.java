package com.kh.investSpring.api.kis.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class StockHistoryCacheDto {

    private Long historyId;

    private String stockCode;
    private String periodType;

    private LocalDate baseDate;

    private Long openPrice;
    private Long highPrice;
    private Long lowPrice;
    private Long closePrice;

    private Long volume;

    private Long changePrice;

    private Double changeRate;
}