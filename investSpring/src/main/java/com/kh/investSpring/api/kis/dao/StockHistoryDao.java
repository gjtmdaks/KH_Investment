package com.kh.investSpring.api.kis.dao;

import java.util.List;

import com.kh.investSpring.api.kis.dto.StockHistoryCacheDto;

public interface StockHistoryDao {

    List<String> selectAllStockCodes();

    void mergeHistory(StockHistoryCacheDto dto);
}