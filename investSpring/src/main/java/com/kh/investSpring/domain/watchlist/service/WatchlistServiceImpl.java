package com.kh.investSpring.domain.watchlist.service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.investSpring.api.kis.service.KisSidebarQuoteEnricher;
import com.kh.investSpring.domain.watchlist.dao.WatchlistDao;
import com.kh.investSpring.domain.watchlist.dto.SidebarWatchDto;
import com.kh.investSpring.domain.watchlist.dto.SidebarWatchResponse;
import com.kh.investSpring.domain.watchlist.dto.WatchlistResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WatchlistServiceImpl implements WatchlistService {

    private static final long PUBLIC_SIDEBAR_CACHE_TTL_MS = 5_000L;
    private static final long USER_SIDEBAR_CACHE_TTL_MS = 3_000L;

    private final WatchlistDao dao;
    private final KisSidebarQuoteEnricher kisSidebarQuoteEnricher;
    private final Object topCurrentPriceCacheLock = new Object();
    private final Object realtimeStocksCacheLock = new Object();
    private final ConcurrentMap<Long, CachedSidebarWatch> sidebarWatchCache =
            new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, CachedSidebarStocks> recentViewsCache =
            new ConcurrentHashMap<>();
    private volatile CachedSidebarStocks topCurrentPriceCache;
    private volatile CachedSidebarStocks realtimeStocksCache;

    @Override
    public void insertWatchlist(Long userNo, String stockCode) {
        if (userNo == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        int count = dao.countWatchlist(userNo);

        if (count >= 50) {
            throw new IllegalStateException(
                "관심종목은 최대 50개까지 가능합니다."
            );
        }

        try {
            dao.insertWatchlist(userNo, stockCode);
            invalidateUserSidebarCache(userNo);

        } catch (DuplicateKeyException e) {
            return;
        }
    }

    @Override
    public void deleteWatchlist(Long userNo, String stockCode) {
        if (userNo == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        dao.deleteWatchlist(userNo, stockCode);
        invalidateUserSidebarCache(userNo);
    }

    @Override
    public WatchlistResponse getWatchlist(Long userNo) {
        return dao.getWatchlist(userNo);
    }

    @Override
    public SidebarWatchResponse getSidebarWatch(Long userNo) {
        if (userNo == null) {
            return SidebarWatchResponse.builder()
                    .loggedIn(false)
                    .hasWatchlist(false)
                    .watchlistCodes(List.of())
                    .stockList(
                        getCachedTopCurrentPriceStocks()
                    )
                    .build();
        }

        long now = System.currentTimeMillis();
        CachedSidebarWatch cached = sidebarWatchCache.get(userNo);

        if (cached != null && cached.expiresAtMs() > now) {
            return cached.response();
        }

        List<String> watchlistCodes = dao.getWatchlist(userNo).getWatchlist();

        List<SidebarWatchDto> watchlist = dao.getSidebarWatchStocks(userNo).stream()
                .map(kisSidebarQuoteEnricher::enrich)
                .toList();

        SidebarWatchResponse response;

        if (watchlistCodes.isEmpty()) {
            response = SidebarWatchResponse.builder()
                    .loggedIn(true)
                    .hasWatchlist(false)
                    .watchlistCodes(List.of())
                    .stockList(
                        getCachedTopCurrentPriceStocks()
                    )
                    .build();
        } else {
            response = SidebarWatchResponse.builder()
                .loggedIn(true)
                .hasWatchlist(true)
                .watchlistCodes(watchlistCodes)
                .stockList(watchlist)
                .build();
        }

        sidebarWatchCache.put(
                userNo,
                new CachedSidebarWatch(
                        response,
                        now + USER_SIDEBAR_CACHE_TTL_MS
                )
        );

        return response;
    }

	@Override
	public List<SidebarWatchDto> getRealtimeStocks() {
		return getCachedRealtimeStocks();
	}

	@Override
	public List<SidebarWatchDto> getRecentViews(Long userNo) {
	    if (userNo == null) {
	        return List.of();
	    }

	    long now = System.currentTimeMillis();
	    CachedSidebarStocks cached = recentViewsCache.get(userNo);

	    if (cached != null && cached.expiresAtMs() > now) {
	        return cached.stocks();
	    }

		List<SidebarWatchDto> stocks = dao.getRecentViews(userNo).stream()
				.map(kisSidebarQuoteEnricher::enrich)
				.toList();

		recentViewsCache.put(
		        userNo,
		        new CachedSidebarStocks(
		                stocks,
		                now + USER_SIDEBAR_CACHE_TTL_MS
		        )
		);

		return stocks;
	}

    @Override
    public void invalidateUserSidebarCache(Long userNo) {
        if (userNo == null) {
            return;
        }

        sidebarWatchCache.remove(userNo);
        recentViewsCache.remove(userNo);
    }

    private List<SidebarWatchDto> getCachedTopCurrentPriceStocks() {
        long now = System.currentTimeMillis();
        CachedSidebarStocks cached = topCurrentPriceCache;

        if (cached != null && cached.expiresAtMs() > now) {
            return cached.stocks();
        }

        synchronized (topCurrentPriceCacheLock) {
            now = System.currentTimeMillis();
            cached = topCurrentPriceCache;

            if (cached != null && cached.expiresAtMs() > now) {
                return cached.stocks();
            }

            List<SidebarWatchDto> stocks = List.copyOf(dao.getTopCurrentPriceStocks());
            topCurrentPriceCache =
                    new CachedSidebarStocks(stocks, now + PUBLIC_SIDEBAR_CACHE_TTL_MS);

            return stocks;
        }
    }

    private List<SidebarWatchDto> getCachedRealtimeStocks() {
        long now = System.currentTimeMillis();
        CachedSidebarStocks cached = realtimeStocksCache;

        if (cached != null && cached.expiresAtMs() > now) {
            return cached.stocks();
        }

        synchronized (realtimeStocksCacheLock) {
            now = System.currentTimeMillis();
            cached = realtimeStocksCache;

            if (cached != null && cached.expiresAtMs() > now) {
                return cached.stocks();
            }

            List<SidebarWatchDto> stocks = List.copyOf(dao.getRealtimeStocks());
            realtimeStocksCache =
                    new CachedSidebarStocks(stocks, now + PUBLIC_SIDEBAR_CACHE_TTL_MS);

            return stocks;
        }
    }

    private record CachedSidebarStocks(
            List<SidebarWatchDto> stocks,
            long expiresAtMs
    ) {
    }

    private record CachedSidebarWatch(
            SidebarWatchResponse response,
            long expiresAtMs
    ) {
    }
}
