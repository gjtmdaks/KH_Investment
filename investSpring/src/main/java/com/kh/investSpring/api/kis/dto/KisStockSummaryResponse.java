package com.kh.investSpring.api.kis.dto;

public record KisStockSummaryResponse(
        String stockCode,
        String stockName,
        String marketId,
        String stockGroup,
        String stockKind,
        String listedShares,
        String capital,
        String parValue,
        String listedDate,
        String fiscalMonth,
        String isKospi200,
        String standardCode,
        String listingAbolitionDate
) {
}
