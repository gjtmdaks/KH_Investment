package com.kh.investSpring.api.kis.dao;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.investSpring.api.kis.dto.StockRealtimeCurrentDto;
import com.kh.investSpring.api.kis.dto.StockRealtimeTickDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StockRealtimeDaoImpl implements StockRealtimeDao {
	
	private final SqlSessionTemplate session;

	@Override
	public void batchInsertTick(List<StockRealtimeTickDto> batch) {
		session.insert("api.batchInsertTick", batch);
	}

	@Override
	public int updateRealtimeCurrent(StockRealtimeTickDto dto) {
		return session.update("api.updateRealtimeCurrent", dto);
	}

	@Override
	public void insertRealtimeCurrent(StockRealtimeTickDto dto) {
		session.insert("api.insertRealtimeCurrent", dto);
	}

	@Override
	public int deleteOldTicks() {
		return session.delete("api.deleteOldTicks");
	}

	@Override
	public StockRealtimeCurrentDto findRealtimeCurrentByStockCode(String stockCode) {
		return session.selectOne("api.findRealtimeCurrentByStockCode", stockCode);
	}

	@Override
	public int updateVolumeAndTradingValue(
			String stockCode,
			Long volume,
			Long tradingValue,
			LocalDateTime updatedAt) {
		Map<String, Object> param = new HashMap<>();
		param.put("stockCode", stockCode);
		param.put("volume", volume);
		param.put("tradingValue", tradingValue);
		param.put("updatedAt", updatedAt);
		return session.update("api.updateVolumeAndTradingValue", param);
	}

}
