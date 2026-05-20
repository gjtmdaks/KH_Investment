package com.kh.investSpring.domain.ai.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.kh.investSpring.domain.ai.dto.StockAiReportDto;
import com.kh.investSpring.domain.ai.dto.StockReportRequestDto;

public interface StockReportDao {

    /**
     * AI 리포트 생성 대상 종목
     */
    List<StockReportRequestDto> getReportTargets();

    /**
     * 종목 최근 뉴스
     */
    List<String> getRecentNews(@Param("stockCode") String stockCode);

    /**
     * AI 리포트 저장
     */
    void upsertStockReport(StockAiReportDto dto);

	StockAiReportDto selectStockReport(String stockCode);
}