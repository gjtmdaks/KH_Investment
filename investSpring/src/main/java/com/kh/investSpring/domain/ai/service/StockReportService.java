package com.kh.investSpring.domain.ai.service;

import com.kh.investSpring.domain.ai.dto.StockAiReportDto;

public interface StockReportService {

    /**
     * GPT 기반 종목 AI 리포트 생성
     */
    void generateReports();

	StockAiReportDto getStockReport(String stockCode);
}