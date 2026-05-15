package com.kh.investSpring.api.kis.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class StockIntradayMinuteCacheDto {

    private Long cacheId;

    private String stockCode;

    private LocalDate tradeDate;

    private String barTime;

    private Long openPrice;
    private Long highPrice;
    private Long lowPrice;
    private Long closePrice;

    private Long volume;
}
