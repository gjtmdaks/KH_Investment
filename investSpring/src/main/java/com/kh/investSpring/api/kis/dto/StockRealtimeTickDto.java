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
    /** 누적 거래량(주) — H0STCNT0 필드 12 */
    private Long volume;
    /** 당일 누적 거래대금(원) — H0STCNT0 필드 13 */
    private Long tradingValue;
    private LocalDateTime tradeTime;

}