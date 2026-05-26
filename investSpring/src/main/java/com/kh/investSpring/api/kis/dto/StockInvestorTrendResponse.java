package com.kh.investSpring.api.kis.dto;

import java.util.List;

public record StockInvestorTrendResponse(
        String stockCode,
        InvestorTrendSummary summary,
        List<InvestorTrendDailyRow> rows,
        String notice) {

    public StockInvestorTrendResponse(
            String stockCode,
            InvestorTrendSummary summary,
            List<InvestorTrendDailyRow> rows) {
        this(stockCode, summary, rows, null);
    }
}
