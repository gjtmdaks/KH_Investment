package com.kh.investSpring.api.kis.dto;

import java.util.List;

public record StockOrderbookViewResponse(
        String stockCode,
        List<KisStockOrderbookResponse.OrderbookLevel> asks,
        List<KisStockOrderbookResponse.OrderbookLevel> bids,
        String totalAskQuantity,
        String totalBidQuantity,
        String expectedPrice,
        String expectedQuantity,
        boolean wsSubscribed,
        StockOrderbookSource orderbookSource,
        String quoteSession,
        String marketDivCode,
        String asOf,
        boolean stale
) {

    public static StockOrderbookViewResponse from(
            KisStockOrderbookResponse orderbook,
            boolean wsSubscribed,
            StockOrderbookSource orderbookSource
    ) {
        if (orderbook == null) {
            return null;
        }
        return new StockOrderbookViewResponse(
                orderbook.stockCode(),
                orderbook.asks(),
                orderbook.bids(),
                orderbook.totalAskQuantity(),
                orderbook.totalBidQuantity(),
                orderbook.expectedPrice(),
                orderbook.expectedQuantity(),
                wsSubscribed,
                orderbookSource,
                null,
                null,
                null,
                false
        );
    }

    public static StockOrderbookViewResponse from(
            KisStockOrderbookResponse orderbook,
            boolean wsSubscribed,
            StockOrderbookSource orderbookSource,
            String quoteSession,
            String marketDivCode,
            String asOf,
            boolean stale
    ) {
        if (orderbook == null) {
            return null;
        }
        return new StockOrderbookViewResponse(
                orderbook.stockCode(),
                orderbook.asks(),
                orderbook.bids(),
                orderbook.totalAskQuantity(),
                orderbook.totalBidQuantity(),
                orderbook.expectedPrice(),
                orderbook.expectedQuantity(),
                wsSubscribed,
                orderbookSource,
                quoteSession,
                marketDivCode,
                asOf,
                stale
        );
    }
}
