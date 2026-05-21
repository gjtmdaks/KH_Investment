package com.kh.investSpring.api.kis.dto;

public record InvestorTrendSummary(
        String tradeDate,
        Long individualNetQty,
        Long foreignNetQty,
        Long institutionNetQty) {
}
