package com.kh.investSpring.domain.watchlist.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecentViewDto {

    private String stockCode;
    private String stockName;

    private Long currentPrice;
    private Double changeRate;

    private Long volume;
    private Long tradingValue;
}