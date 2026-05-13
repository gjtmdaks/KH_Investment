package com.kh.investSpring.domain.watchlist.service;

import com.kh.investSpring.domain.watchlist.dto.WatchlistResponse;

public interface WatchlistService {
	
    void insertWatchlist(Long userNo, String stockCode);

    void deleteWatchlist(Long userNo, String stockCode);

    WatchlistResponse getWatchlist(Long userNo);

}
