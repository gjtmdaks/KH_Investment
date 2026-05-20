package com.kh.investSpring.domain.stock.dto;

import lombok.Data;

@Data
public class StockKeywordSearchDto {

    private String stockCode;
    private String stockName;
    private String marketType;
    private Integer matchScore;
    private Long tradingValue;
}
