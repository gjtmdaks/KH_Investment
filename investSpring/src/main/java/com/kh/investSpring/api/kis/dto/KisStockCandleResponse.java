package com.kh.investSpring.api.kis.dto;

import java.util.List;

public record KisStockCandleResponse(
        String stockCode,
        String period,
        String from,
        String to,
        List<KisStockCandleItemResponse> candles
) {
}
