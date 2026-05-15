package com.kh.investSpring.api.kis.dao;

import java.time.LocalDate;
import java.util.List;

import com.kh.investSpring.api.kis.dto.StockIntradayMinuteCacheDto;

public interface StockIntradayMinuteDao {

    List<StockIntradayMinuteCacheDto> selectByStockAndDate(String stockCode, LocalDate tradeDate);

    void mergeMinute(StockIntradayMinuteCacheDto dto);
}
