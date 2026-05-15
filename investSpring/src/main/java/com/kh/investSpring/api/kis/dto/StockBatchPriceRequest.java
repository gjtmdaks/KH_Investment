package com.kh.investSpring.api.kis.dto;

import java.util.List;

public record StockBatchPriceRequest(List<String> stockCodes) {
}
