package com.kh.investSpring.api.kis.dao;

import java.util.List;

import com.kh.investSpring.api.kis.dto.KisStockPriceResponse;
import com.kh.investSpring.api.kis.dto.StockRealtimeCurrentDto;
import com.kh.investSpring.api.kis.dto.StockRealtimeTickDto;

public interface StockRealtimeDao {

	void batchInsertTick(List<StockRealtimeTickDto> batch);
	
	int updateRealtimeCurrent(List<StockRealtimeTickDto> batch);
	
	void insertRealtimeCurrent(StockRealtimeTickDto dto);

	int deleteOldTicks();

	StockRealtimeCurrentDto findRealtimeCurrentByStockCode(String stockCode);

	int updateVolumeAndTradingValue(
			String stockCode,
			Long volume,
			Long tradingValue,
			java.time.LocalDateTime updatedAt);

	void refreshCurrentFromRest(KisStockPriceResponse response);

	List<String> selectStaleStockCodes();

}
