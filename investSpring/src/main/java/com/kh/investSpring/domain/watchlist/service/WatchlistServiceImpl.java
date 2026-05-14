package com.kh.investSpring.domain.watchlist.service;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.investSpring.domain.watchlist.dao.WatchlistDao;
import com.kh.investSpring.domain.watchlist.dto.RecentViewDto;
import com.kh.investSpring.domain.watchlist.dto.SidebarWatchDto;
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

        int count = dao.countWatchlist(userNo);

        if (count >= 50) {
            throw new IllegalStateException(
                "관심종목은 최대 50개까지 가능합니다."
            );
        }

        try {
            dao.insertWatchlist(userNo, stockCode);

        } catch (DuplicateKeyException e) {
            // 이미 존재하면 무시
            return;
        }
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
                    .watchlistCodes(List.of())
                    .stockList(
                        dao.getTopCurrentPriceStocks()
                    )
                    .build();
        }

        // 실제 관심종목 코드
        List<String> watchlistCodes = dao.getWatchlist(userNo).getWatchlist();

        // 관심종목 상세
        List<SidebarWatchDto> watchlist = dao.getSidebarWatchStocks(userNo);

        // 관심종목 없음
        if (watchlistCodes.isEmpty()) {
            return SidebarWatchResponse.builder()
                    .loggedIn(true)
                    .hasWatchlist(false)
                    .watchlistCodes(List.of())
                    .stockList(
                        dao.getTopCurrentPriceStocks()
                    )
                    .build();
        }

        // 관심종목 존재
        return SidebarWatchResponse.builder()
                .loggedIn(true)
                .hasWatchlist(true)
                .watchlistCodes(watchlistCodes)
                .stockList(watchlist)
                .build();
    }

	@Override
	public List<SidebarWatchDto> getRealtimeStocks() {
		return dao.getRealtimeStocks();
	}

	@Override
	public List<RecentViewDto> getRecentViews(Long userNo) {
	    if (userNo == null) {
	        return List.of();
	    }
		return dao.getRecentViews(userNo);
	}
}