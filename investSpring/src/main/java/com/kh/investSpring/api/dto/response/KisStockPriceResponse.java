package com.kh.investSpring.api.dto.response;

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
