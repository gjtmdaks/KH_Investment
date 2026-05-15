package com.kh.investSpring.api.kis.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.kh.investSpring.api.kis.dto.StockHistoryCacheDto;

public interface StockHistoryDao {

    List<String> selectAllStockCodes();

    void mergeHistory(StockHistoryCacheDto dto);

    List<StockHistoryCacheDto> selectHistoryByRange(
            String stockCode,
            String periodType,
            LocalDate fromDate,
            LocalDate toDate
    );

    void mergeFetchState(String stockCode, String periodType);

    Map<String, Object> selectFetchState();

    void clearFetchState();
}