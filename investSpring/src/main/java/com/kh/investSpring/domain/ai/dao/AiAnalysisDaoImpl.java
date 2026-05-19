package com.kh.investSpring.domain.ai.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.investSpring.domain.ai.dto.NewsAnalysisDto;
import com.kh.investSpring.domain.ai.dto.NewsAnalysisTargetDto;
import com.kh.investSpring.domain.ai.dto.StockNewsAnalysisDto;
import com.kh.investSpring.domain.stock.dto.StockAiSummaryDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AiAnalysisDaoImpl implements AiAnalysisDao {
	
	private final SqlSessionTemplate session;

	@Override
	public List<NewsAnalysisTargetDto> getUnAnalyzedNews() {
		return session.selectList("ai.getUnAnalyzedNews");
	}

	@Override
	public void insertNewsAnalysis(NewsAnalysisDto dto) {
		session.insert("ai.insertNewsAnalysis", dto);
	}
	
	@Override
	public List<StockNewsAnalysisDto> getStockNewsAnalysisTargets() {
	    return session.selectList("ai.getStockNewsAnalysisTargets");
	}

	@Override
	public void mergeStockAiSummary(StockAiSummaryDto dto) {
	    session.insert("ai.mergeStockAiSummary", dto);
	}
	
}
