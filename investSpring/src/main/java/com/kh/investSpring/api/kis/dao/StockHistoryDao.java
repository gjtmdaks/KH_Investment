package com.kh.investSpring.api.kis.dao;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.kh.investSpring.api.kis.dto.StockHistoryCacheDto;

public interface StockHistoryDao {

    List<String> selectAllStockCodes();

    void mergeHistory(StockHistoryCacheDto dto);

    List<StockHistoryCacheDto> selectHistoryByRange(
            @Param("stockCode") String stockCode,
            @Param("periodType") String periodType,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}