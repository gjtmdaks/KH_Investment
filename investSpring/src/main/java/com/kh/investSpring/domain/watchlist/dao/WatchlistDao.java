package com.kh.investSpring.domain.watchlist.dao;

import com.kh.investSpring.domain.watchlist.dto.WatchlistResponse;

public interface WatchlistDao {

    int insertWatchlist(Long userNo, String stockCode);

    int deleteWatchlist(Long userNo, String stockCode);

    WatchlistResponse getWatchlist(Long userNo);

	boolean existsWatchlist(Long userNo, String stockCode);

}