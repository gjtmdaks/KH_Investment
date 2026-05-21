package com.kh.investSpring.domain.stock.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.investSpring.domain.stock.dto.StockDto;
import com.kh.investSpring.domain.stock.dto.StockInfoDto;
import com.kh.investSpring.domain.stock.dto.StockKeywordSearchDto;
import com.kh.investSpring.domain.stock.dto.StockScreenerDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StockDaoImpl implements StockDao {
	
	private final SqlSessionTemplate session;

	@Override
	public List<StockDto> getStockList() {
		return getStockList(5);
	}

	@Override
	public List<StockDto> getStockList(int realtimeFreshMinutes) {
		int fresh = Math.max(1, realtimeFreshMinutes);
		Map<String, Object> param = new HashMap<>();
		param.put("freshMinutes", fresh);
		return session.selectList("stock.getStockList", param);
	}

	@Override
	public String getTopVolumeStockCode() {
		return session.selectOne("stock.getTopVolumeStockCode");
	}

	@Override
	public StockInfoDto getStockInfo(String stockCode) {
		return session.selectOne("stock.getStockInfo", stockCode);
	}

	@Override
	public List<Long> getMiniChart(String stockCode) {
		return session.selectList("stock.getMiniChart", stockCode);
	}

	@Override
	public List<String> findAllStockCodes() {
		return session.selectList("stock.findAllStockCodes");
	}

	@Override
	public List<String> selectTopTradingValueStockCodes(int limit, int freshMinutes) {
		Map<String, Object> param = new HashMap<>();
		param.put("limit", limit);
		param.put("freshMinutes", Math.max(1, freshMinutes));
		return session.selectList("stock.selectTopTradingValueStockCodes", param);
	}

	@Override
	public List<String> selectRecentViewDemandStockCodes(int limit, int recentMinutes) {
		Map<String, Object> param = new HashMap<>();
		param.put("limit", limit);
		param.put("recentMinutes", recentMinutes);
		return session.selectList("stock.selectRecentViewDemandStockCodes", param);
	}

	@Override
	public List<String> selectWatchlistDemandStockCodes(int limit) {
		return session.selectList("stock.selectWatchlistDemandStockCodes", limit);
	}
	
	@Override
	public List<StockScreenerDto> getRisingStocks() {
        return session.selectList("stock.getRisingStocks");
    }

	@Override
    public List<StockScreenerDto> getFallingStocks() {
        return session.selectList("stock.getFallingStocks");
    }

	@Override
    public List<StockScreenerDto> getPopularWatchlistStocks() {
        return session.selectList("stock.getPopularWatchlistStocks");
    }

	@Override
    public List<StockScreenerDto> getViewedStocks() {
        return session.selectList("stock.getViewedStocks");
    }

	@Override
    public List<StockScreenerDto> getVolumeStocks() {
        return session.selectList("stock.getVolumeStocks");
    }
	
	@Override
	public List<StockScreenerDto> searchStocks(String market, String changeRate, String volume) {
	    Map<String, Object> param = new HashMap<>();

	    param.put("market", market);
	    param.put("changeRate", changeRate);
	    param.put("volume", volume);

	    return session.selectList("stock.searchStocks", param);
	}

	@Override
	public List<StockKeywordSearchDto> searchStocksByKeyword(Map<String, Object> params) {
	    return session.selectList("stock.searchStocksByKeyword", params);
	}
	
	@Override
	public List<StockScreenerDto> getRealtimeSurgingStocks() {
	    return session.selectList("stock.getRealtimeSurgingStocks");
	}

	@Override
	public List<StockScreenerDto> getRealtimeFallingStocks() {
	    return session.selectList("stock.getRealtimeFallingStocks");
	}

	@Override
	public List<StockScreenerDto> getRealtimeActiveStocks() {
	    return session.selectList("stock.getRealtimeActiveStocks");
	}

	@Override
	public StockDto findByStockCode(String stockCode) {
		return session.selectOne("stock.findByStockCode", stockCode);
	}
	
	@Override
    public boolean existsByStockCode(String stockCode) {
        Integer count = session.selectOne("stock.existsByStockCode", stockCode);
        return count != null && count > 0;
    }

}
