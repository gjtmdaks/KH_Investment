package com.kh.investSpring.domain.stock.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kh.investSpring.domain.stock.dao.StockDao;
import com.kh.investSpring.domain.stock.dto.RealtimeSectionResponseDto;
import com.kh.investSpring.domain.stock.dto.StockDto;
import com.kh.investSpring.domain.stock.dto.StockKeywordSearchDto;
import com.kh.investSpring.domain.stock.dto.StockScreenerDto;
import com.kh.investSpring.domain.stock.dto.TopStockDto;
import com.kh.investSpring.domain.stock.service.StockSearchKeywordResolver.ResolvedQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockDao stockDao;
    private final StockSearchKeywordResolver searchKeywordResolver;

    @Override
    public List<StockDto> getStockList() {
        List<StockDto> stocks = stockDao.getStockList();

        if (stocks == null) {
            return List.of();
        }

        return stocks.stream()
                .map(stock -> {
                    if (stock.getAiSummary() == null
                            || stock.getAiSummary().isBlank()) {
                        stock.setAiSummary("⏳ AI 분석 대기중");
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

    @Override
    public List<StockKeywordSearchDto> searchByKeyword(String keyword, int limit) {
        ResolvedQuery query = searchKeywordResolver.resolve(keyword);
        if (query.keywords().isEmpty()) {
            return List.of();
        }

        int safeLimit = Math.min(Math.max(limit, 1), 100);

        Map<String, Object> params = new HashMap<>();
        params.put("primaryKeyword", query.primaryKeyword());
        params.put("keywords", query.keywords());
        params.put("boostNames", query.boostCanonicalNames());
        params.put("searchStockCode", query.searchStockCode());
        params.put("limit", safeLimit + 15);

        List<StockKeywordSearchDto> raw = stockDao.searchStocksByKeyword(params);
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }

        List<StockKeywordSearchDto> deduped =
                StockSearchKeywordResolver.dedupePreferCommonStock(raw);

        return deduped.size() <= safeLimit
                ? deduped
                : deduped.subList(0, safeLimit);
    }
    
    public RealtimeSectionResponseDto getRealtimeSection() {

        return RealtimeSectionResponseDto.builder()
                .surging(stockDao.getRealtimeSurgingStocks())
                .falling(stockDao.getRealtimeFallingStocks())
                .active(stockDao.getRealtimeActiveStocks())
                .build();
    }
}