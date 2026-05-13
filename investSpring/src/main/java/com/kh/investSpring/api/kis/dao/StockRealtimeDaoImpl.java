package com.kh.investSpring.api.kis.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

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
	public void deleteOldTicks() {
		session.delete("api.deleteOldTicks");
	}

}
