package com.kh.investSpring.domain.stock.dto;

import lombok.Getter;

@Getter
public class StockInfoDto {

    private String stockCode;
    private String stockName;
    private Long price;
    private Double changeRate;
}