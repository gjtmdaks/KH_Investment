package com.kh.investSpring.domain.stock.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.investSpring.domain.stock.dto.StockDto;
import com.kh.investSpring.domain.stock.dto.StockInfoDto;
import com.kh.investSpring.domain.stock.dto.StockScreenerDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StockDaoImpl implements StockDao {
	
	private final SqlSessionTemplate session;

	@Override
	public List<StockDto> getStockList() {
		return session.selectList("stock.getStockList");
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

}
