package com.kh.investSpring.domain.watchlist.service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.investSpring.api.kis.dto.KisStockPriceResponse;
import com.kh.investSpring.api.kis.service.KisStockService;
import com.kh.investSpring.domain.watchlist.dao.WatchlistDao;
import com.kh.investSpring.domain.watchlist.dto.SidebarWatchDto;
import com.kh.investSpring.domain.watchlist.dto.SidebarWatchResponse;
import com.kh.investSpring.domain.watchlist.dto.WatchlistResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WatchlistServiceImpl implements WatchlistService {

    private static final long KIS_SIDEBAR_QUOTE_CACHE_MS = 30_000L;

    private final WatchlistDao dao;
    private final KisStockService kisStockService;
    private final ConcurrentHashMap<String, CachedKisSidebarQuote> kisSidebarQuoteCache =
            new ConcurrentHashMap<>();

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
	public List<SidebarWatchDto> getRecentViews(Long userNo) {
	    if (userNo == null) {
	        return List.of();
	    }
		return dao.getRecentViews(userNo).stream()
				.map(this::fillMissingQuoteFromKis)
				.toList();
	}

	private SidebarWatchDto fillMissingQuoteFromKis(SidebarWatchDto dto) {
		if (dto.getCurrentPrice() != null && dto.getCurrentPrice() > 0) {
			return dto;
		}

		String stockCode = dto.getStockCode();
		long now = System.currentTimeMillis();
		CachedKisSidebarQuote cached = kisSidebarQuoteCache.get(stockCode);
		if (cached != null && cached.expiresAtMs() > now) {
			return cached.quote();
		}

		try {
			KisStockPriceResponse price = kisStockService.getStockPrice(stockCode);
			Long currentPrice = parseLong(price.currentPrice());
			Long volume = parseLong(price.volume());
			Long tradingValue = parseLong(price.tradingValue());

			if (tradingValue == null && currentPrice != null && volume != null) {
				tradingValue = currentPrice * volume;
			}

			String stockName = price.stockName();
			if (stockName == null || stockName.isBlank()) {
				stockName = dto.getStockName();
			}

			SidebarWatchDto enriched = SidebarWatchDto.builder()
					.stockCode(dto.getStockCode())
					.stockName(stockName)
					.currentPrice(currentPrice != null ? currentPrice : 0L)
					.changeRate(parseDouble(price.changeRate()))
					.volume(volume != null ? volume : 0L)
					.tradingValue(tradingValue != null ? tradingValue : 0L)
					.build();
			kisSidebarQuoteCache.put(
					stockCode,
					new CachedKisSidebarQuote(enriched, now + KIS_SIDEBAR_QUOTE_CACHE_MS));
			return enriched;
		} catch (Exception e) {
			log.debug("KIS 시세 보완 실패 - stockCode={}", dto.getStockCode(), e);
			return withZeroQuote(dto);
		}
	}

	private record CachedKisSidebarQuote(SidebarWatchDto quote, long expiresAtMs) {
	}

	private static SidebarWatchDto withZeroQuote(SidebarWatchDto dto) {
		return SidebarWatchDto.builder()
				.stockCode(dto.getStockCode())
				.stockName(dto.getStockName())
				.currentPrice(0L)
				.changeRate(0.0)
				.volume(0L)
				.tradingValue(0L)
				.build();
	}

	private static Long parseLong(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return Long.parseLong(value.replace(",", "").trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static Double parseDouble(String value) {
		if (value == null || value.isBlank()) {
			return 0.0;
		}
		try {
			return Double.parseDouble(value.replace(",", "").trim());
		} catch (NumberFormatException e) {
			return 0.0;
		}
	}
}