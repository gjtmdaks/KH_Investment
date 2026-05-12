package com.kh.investSpring.api.kis.dto;

public record KisStockDetailResponse(
        KisStockPriceResponse price,
        KisStockSummaryResponse summary
) {
}
