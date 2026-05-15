package com.kh.investSpring.domain.stock.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.kh.investSpring.domain.stock.dto.StockDto;
import com.kh.investSpring.domain.stock.dto.StockInfoDto;
import com.kh.investSpring.domain.stock.dto.StockScreenerDto;

public interface StockDao {

    // ✅ 메인 리스트
    List<StockDto> getStockList();

    // ✅ 거래대금 1위 종목 코드
    String getTopVolumeStockCode();

    // ✅ 종목 기본 정보
    StockInfoDto getStockInfo(@Param("stockCode") String stockCode);

    // ✅ 미니 차트
    List<Long> getMiniChart(@Param("stockCode") String stockCode);
    
    List<String> findAllStockCodes();
    
    public List<StockScreenerDto> getRisingStocks();
    
    public List<StockScreenerDto> getFallingStocks();
    
    public List<StockScreenerDto> getPopularWatchlistStocks();
    
    public List<StockScreenerDto> getViewedStocks();
    
    public List<StockScreenerDto> getVolumeStocks();

	List<StockScreenerDto> searchStocks(String market, String changeRate, String volume);

	List<StockScreenerDto> getRealtimeSurgingStocks();

	List<StockScreenerDto> getRealtimeFallingStocks();

	List<StockScreenerDto> getRealtimeActiveStocks();
    
}