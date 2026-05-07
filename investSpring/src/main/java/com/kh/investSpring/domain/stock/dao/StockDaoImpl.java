package com.kh.investSpring.domain.stock.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.investSpring.domain.stock.dto.StockDto;
import com.kh.investSpring.domain.stock.dto.StockInfoDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StockDaoImpl implements StockDao {
	
	private final SqlSessionTemplate session;

	@Override
	public List<StockDto> getStockList() {
		return session.selectList("stock.getStockList");
	}

	@Override
	public String getTopVolumeStockCode() {
		return session.selectOne("stock.getTopVolumeStockCode");
	}

	@Override
	public StockInfoDto getStockInfo(String stockCode) {
		return session.selectOne("stock.getStockInfo", stockCode);
	}

	@Override
	public List<Long> getMiniChart(String stockCode) {
		return session.selectList("stock.getMiniChart", stockCode);
	}

	@Override
	public List<String> findAllStockCodes() {
		return session.selectList("stock.findAllStockCodes");
	}

}
