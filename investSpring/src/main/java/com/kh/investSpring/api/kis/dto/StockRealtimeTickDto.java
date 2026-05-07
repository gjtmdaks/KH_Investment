package com.kh.investSpring.api.kis.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockRealtimeTickDto {

    private String stockCode;
    private Long currentPrice;
    private Double changeRate;
    private Long volume;
    private LocalDateTime tradeTime;

}