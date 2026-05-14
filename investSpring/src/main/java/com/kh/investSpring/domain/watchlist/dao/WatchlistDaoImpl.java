package com.kh.investSpring.domain.watchlist.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.investSpring.domain.watchlist.dto.SidebarWatchDto;
import com.kh.investSpring.domain.watchlist.dto.WatchlistResponse;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WatchlistDaoImpl implements WatchlistDao {

    private final SqlSessionTemplate session;

	@Override
	public int countWatchlist(Long userNo) {
		return session.selectOne("watchlist.countWatchlist", userNo);
	}

    @Override
    public int insertWatchlist(Long userNo, String stockCode) {
        Map<String, Object> param = new HashMap<>();
        param.put("userNo", userNo);
        param.put("stockCode", stockCode);

        return session.insert("watchlist.insertWatchlist", param);
    }

    @Override
    public int deleteWatchlist(Long userNo, String stockCode) {
        Map<String, Object> param = new HashMap<>();
        param.put("userNo", userNo);
        param.put("stockCode", stockCode);

        return session.delete("watchlist.deleteWatchlist", param);
    }

    @Override
    public WatchlistResponse getWatchlist(Long userNo) {
        List<String> watchlist = session.selectList("watchlist.getWatchlist", userNo);

        return new WatchlistResponse(watchlist);
    }

	@Override
	public List<SidebarWatchDto> getTopCurrentPriceStocks() {
		return session.selectList("watchlist.getTopCurrentPriceStocks");
	}

	@Override
	public List<SidebarWatchDto> getSidebarWatchStocks(Long userNo) {
		return session.selectList("watchlist.getSidebarWatchStocks", userNo);
	}
}