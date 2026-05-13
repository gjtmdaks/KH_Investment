package com.kh.investSpring.domain.watchlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SidebarWatchDto {

    private String stockCode;
    private String stockName;

    private Long currentPrice;
    private Double changeRate;

    private Long volume;
    private Long tradingValue;
    
}
