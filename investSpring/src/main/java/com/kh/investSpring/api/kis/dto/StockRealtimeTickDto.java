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
    /** 전일 대비 가격 — H0STCNT0 필드 4 (PRDY_VRSS) */
    private Long changePrice;
    private Double changeRate;
    /** 시가 — H0STCNT0 필드 7 (STCK_OPRC) */
    private Long openPrice;
    /** 당일 누적 거래량·거래대금은 KIS REST backfill 전용 (WS tick 미사용) */
    private Long volume;
    private Long tradingValue;
    private LocalDateTime tradeTime;
    private String quoteSource;

}