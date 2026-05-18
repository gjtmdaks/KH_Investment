package com.kh.investSpring.domain.search.dto;

import lombok.Data;

@Data
public class SearchStockResponse {

    private String stockCode;
    private String stockName;
    private String marketType;
}