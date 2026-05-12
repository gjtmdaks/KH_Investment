package com.kh.investSpring.api.kis.dto;

import java.util.List;

public record KisStockOrderbookResponse(
        String stockCode,
        List<OrderbookLevel> asks,
        List<OrderbookLevel> bids,
        String totalAskQuantity,
        String totalBidQuantity,
        String expectedPrice,
        String expectedQuantity
) {

    public record OrderbookLevel(
            int level,
            String price,
            String quantity,
            String quantityChange
    ) {
    }
}
