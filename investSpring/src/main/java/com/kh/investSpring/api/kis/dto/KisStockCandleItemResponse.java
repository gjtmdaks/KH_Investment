package com.kh.investSpring.api.kis.dto;

public record KisStockCandleItemResponse(
        String date,
        long open,
        long high,
        long low,
        long close,
        long volume
) {
}
