package com.kh.investSpring.api.kis.dto;

public record KisStockPriceResponse(
		String stockCode,
        String stockName,
        String currentPrice,
        String changePrice,
        String changeRate,
        String volume,
        String tradingValue,
        String openPrice,
        String highPrice,
        String lowPrice
		) {

}
