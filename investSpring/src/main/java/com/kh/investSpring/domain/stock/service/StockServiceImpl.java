package com.kh.investSpring.domain.stock.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kh.investSpring.domain.board.dao.BoardDao;
import com.kh.investSpring.domain.stock.dao.StockDao;
import com.kh.investSpring.domain.stock.dto.RealtimeSectionResponseDto;
import com.kh.investSpring.domain.stock.dto.StockDto;
import com.kh.investSpring.domain.stock.dto.StockScreenerDto;
import com.kh.investSpring.domain.stock.dto.TopStockDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockDao stockDao;
    private final BoardDao boardDao;

    @Override
    public List<StockDto> getStockList() {
        List<StockDto> stocks = stockDao.getStockList();

        if (stocks == null) {
            return List.of();
        }

        return stocks.stream()
                .map(stock -> {
                    // AI 분석 없을 경우 기본값
                    if (stock.getAiSentiment() == null) {
                        stock.setAiSentiment("NEUTRAL");
                    }

                    if (stock.getAiSummary() == null
                            || stock.getAiSummary().isBlank()) {
                        stock.setAiSummary("AI 분석 대기중");
                    }

                    return stock;
                })
                .toList();
    }

    @Override
    public TopStockDto getTopVolumeStock() {

        // 1️⃣ 거래대금 1위
        String stockCode = stockDao.getTopVolumeStockCode();

        // 2️⃣ 기본 정보
        var info = stockDao.getStockInfo(stockCode);

        // 3️⃣ 차트 데이터
        List<Long> chart = stockDao.getMiniChart(stockCode);

        return TopStockDto.builder()
                .stockCode(info.getStockCode())
                .stockName(info.getStockName())
                .price(info.getPrice())
                .changeRate(info.getChangeRate())
                .miniChart(chart)
                .build();
    }
    
    public List<StockScreenerDto> getRisingStocks() {
        return stockDao.getRisingStocks();
    }

    public List<StockScreenerDto> getFallingStocks() {
        return stockDao.getFallingStocks();
    }

    public List<StockScreenerDto> getPopularWatchlistStocks() {
        return stockDao.getPopularWatchlistStocks();
    }

    public List<StockScreenerDto> getViewedStocks() {
        return stockDao.getViewedStocks();
    }

    public List<StockScreenerDto> getVolumeStocks() {
        return stockDao.getVolumeStocks();
    }
    
    public List<StockScreenerDto> searchStocks(String market, String changeRate, String volume) {
        return stockDao.searchStocks(market, changeRate, volume);
    }
    
    public RealtimeSectionResponseDto getRealtimeSection() {

        return RealtimeSectionResponseDto.builder()
                .surging(stockDao.getRealtimeSurgingStocks())
                .falling(stockDao.getRealtimeFallingStocks())
                .active(stockDao.getRealtimeActiveStocks())
                .build();
    }
}