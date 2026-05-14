package com.kh.investSpring.domain.watchlist.dao;

import java.util.List;

import com.kh.investSpring.domain.watchlist.dto.SidebarWatchDto;
import com.kh.investSpring.domain.watchlist.dto.WatchlistResponse;

public interface WatchlistDao {

	int countWatchlist(Long userNo);
	
    int insertWatchlist(Long userNo, String stockCode);

    int deleteWatchlist(Long userNo, String stockCode);

    WatchlistResponse getWatchlist(Long userNo);

	List<SidebarWatchDto> getTopCurrentPriceStocks();

	List<SidebarWatchDto> getSidebarWatchStocks(Long userNo);

	List<SidebarWatchDto> getRealtimeStocks();
	
	List<SidebarWatchDto> getRecentViews(Long userNo);

}