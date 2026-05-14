package com.kh.investSpring.api.kis.dto;

import com.kh.investSpring.api.dart.dto.StockStaticProfileResponse;

public record KisStockDetailResponse(
        KisStockPriceResponse price,
        StockStaticProfileResponse profile
) {
}
