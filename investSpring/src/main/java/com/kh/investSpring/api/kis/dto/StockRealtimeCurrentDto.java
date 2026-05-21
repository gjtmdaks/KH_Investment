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
public class StockRealtimeCurrentDto {

    private String stockCode;
    private String stockName;
    private Long currentPrice;
    private Long openPrice;
    private Long changePrice;
    private Double changeRate;
    private Long volume;
    private LocalDateTime updatedAt;
}
