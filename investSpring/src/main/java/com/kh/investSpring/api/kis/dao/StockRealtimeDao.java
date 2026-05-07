package com.kh.investSpring.api.kis.dao;

import java.util.List;

import com.kh.investSpring.api.kis.dto.StockRealtimeTickDto;

public interface StockRealtimeDao {

	void batchInsertTick(List<StockRealtimeTickDto> batch);

}
