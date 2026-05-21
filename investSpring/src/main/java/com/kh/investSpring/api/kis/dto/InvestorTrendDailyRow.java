package com.kh.investSpring.api.kis.dto;

public record InvestorTrendDailyRow(
        String tradeDate,
        Long individualNetQty,
        Long foreignNetQty,
        Long institutionNetQty) {
}
