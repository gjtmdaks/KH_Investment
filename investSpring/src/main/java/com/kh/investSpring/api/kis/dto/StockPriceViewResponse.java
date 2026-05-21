package com.kh.investSpring.api.kis.dto;

public record StockPriceViewResponse(
        String stockCode,
        String stockName,
        String currentPrice,
        String changePrice,
        String changeRate,
        String volume,
        String tradingValue,
        String openPrice,
        String highPrice,
        String lowPrice,
        String executionStrength,
        boolean wsSubscribed,
        StockPriceSource priceSource
) {

    public static StockPriceViewResponse from(
            KisStockPriceResponse price,
            boolean wsSubscribed,
            StockPriceSource priceSource
    ) {
        if (price == null) {
            return null;
        }
        return new StockPriceViewResponse(
                price.stockCode(),
                price.stockName(),
                price.currentPrice(),
                price.changePrice(),
                price.changeRate(),
                price.volume(),
                price.tradingValue(),
                price.openPrice(),
                price.highPrice(),
                price.lowPrice(),
                price.executionStrength(),
                wsSubscribed,
                priceSource
        );
    }
}
