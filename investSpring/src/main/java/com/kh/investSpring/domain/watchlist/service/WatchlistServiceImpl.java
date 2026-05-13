package com.kh.investSpring.domain.watchlist.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.investSpring.domain.watchlist.dao.WatchlistDao;
import com.kh.investSpring.domain.watchlist.dto.SidebarWatchResponse;
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

	@Override
	public SidebarWatchResponse getSidebarWatch(Long userNo) {
        // 비로그인
        if (userNo == null) {
            return SidebarWatchResponse.builder()
                    .loggedIn(false)
                    .hasWatchlist(false)
                    .stockList(dao.getTopCurrentPriceStocks())
                    .build();
        }

        List<String> watchlist = dao.getWatchlistCodes(userNo);

        // 관심종목 없음
        if (watchlist == null || watchlist.isEmpty()) {
            return SidebarWatchResponse.builder()
                    .loggedIn(true)
                    .hasWatchlist(false)
                    .stockList(dao.getTopCurrentPriceStocks())
                    .build();
        }

        // 관심종목 존재
        return SidebarWatchResponse.builder()
                .loggedIn(true)
                .hasWatchlist(true)
                .stockList(dao.getSidebarWatchStocks(userNo))
                .build();
	}
}