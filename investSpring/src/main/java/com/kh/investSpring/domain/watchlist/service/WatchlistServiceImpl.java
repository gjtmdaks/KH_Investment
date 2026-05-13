package com.kh.investSpring.domain.watchlist.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.investSpring.domain.watchlist.dao.WatchlistDao;
import com.kh.investSpring.domain.watchlist.dto.WatchlistResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WatchlistServiceImpl implements WatchlistService {

    private final WatchlistDao dao;

    @Override
    public void insertWatchlist(Long userNo, String stockCode) {
    	if (dao.existsWatchlist(userNo, stockCode)) {
            return;
        }
    	
        dao.insertWatchlist(userNo, stockCode);
    }

    @Override
    public void deleteWatchlist(Long userNo, String stockCode) {
        dao.deleteWatchlist(userNo, stockCode);
    }

    @Override
    public WatchlistResponse getWatchlist(Long userNo) {
        return dao.getWatchlist(userNo);
    }
}