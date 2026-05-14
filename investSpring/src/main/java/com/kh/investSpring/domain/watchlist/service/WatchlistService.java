package com.kh.investSpring.domain.watchlist.service;

import java.util.List;

import com.kh.investSpring.domain.watchlist.dto.RecentViewDto;
import com.kh.investSpring.domain.watchlist.dto.SidebarWatchDto;
import com.kh.investSpring.domain.watchlist.dto.SidebarWatchResponse;
import com.kh.investSpring.domain.watchlist.dto.WatchlistResponse;

public interface WatchlistService {
	
    void insertWatchlist(Long userNo, String stockCode);

    void deleteWatchlist(Long userNo, String stockCode);

    WatchlistResponse getWatchlist(Long userNo);

	SidebarWatchResponse getSidebarWatch(Long userNo);

	List<SidebarWatchDto> getRealtimeStocks();
	
	List<RecentViewDto> getRecentViews(Long userNo);

}
