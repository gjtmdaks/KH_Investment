package com.kh.investSpring.domain.ai.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.investSpring.domain.ai.dto.StockAiReportDto;
import com.kh.investSpring.domain.ai.dto.StockReportRequestDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StockReportDaoImpl implements StockReportDao {
	
	private final SqlSessionTemplate session;

	@Override
	public List<StockReportRequestDto> getReportTargets() {
		return session.selectList("ai.getReportTargets");
	}

	@Override
	public List<String> getRecentNews(String stockCode) {
		return session.selectList("ai.getRecentNews", stockCode);
	}

	@Override
	public void upsertStockReport(StockAiReportDto dto) {
		session.insert("ai.upsertStockReport", dto);
	}

	@Override
	public StockAiReportDto selectStockReport(String stockCode) {
		return session.selectOne("ai.selectStockReport", stockCode);
	}

}
