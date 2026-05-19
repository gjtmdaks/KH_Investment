package com.kh.investSpring.domain.ai.dao;

import java.util.List;

import com.kh.investSpring.domain.ai.dto.NewsAnalysisDto;
import com.kh.investSpring.domain.ai.dto.NewsAnalysisTargetDto;
import com.kh.investSpring.domain.ai.dto.StockNewsAnalysisDto;
import com.kh.investSpring.domain.stock.dto.StockAiSummaryDto;

public interface AiAnalysisDao {

    List<NewsAnalysisTargetDto> getUnAnalyzedNews();
    
    void insertNewsAnalysis(NewsAnalysisDto dto);
    
    List<StockNewsAnalysisDto> getStockNewsAnalysisTargets();

    void mergeStockAiSummary(StockAiSummaryDto dto);
}