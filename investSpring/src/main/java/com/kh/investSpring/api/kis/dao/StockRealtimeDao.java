package com.kh.investSpring.api.kis.dao;

import java.util.List;

import com.kh.investSpring.api.kis.dto.StockRealtimeCurrentDto;
import com.kh.investSpring.api.kis.dto.StockRealtimeTickDto;

public interface StockRealtimeDao {

	void batchInsertTick(List<StockRealtimeTickDto> batch);
	
	int updateRealtimeCurrent(StockRealtimeTickDto dto);
	
	void insertRealtimeCurrent(StockRealtimeTickDto dto);

	void deleteOldTicks();

	StockRealtimeCurrentDto findRealtimeCurrentByStockCode(String stockCode);

}
