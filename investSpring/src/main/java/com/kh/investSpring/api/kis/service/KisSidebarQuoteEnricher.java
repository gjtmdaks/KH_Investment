package com.kh.investSpring.api.kis.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.kh.investSpring.api.kis.dto.KisStockPriceResponse;
import com.kh.investSpring.domain.account.dto.AccountAssetResponse;
import com.kh.investSpring.domain.watchlist.dto.SidebarWatchDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisSidebarQuoteEnricher {

    private static final long KIS_SIDEBAR_QUOTE_CACHE_MS = 30_000L;

    private final KisStockService kisStockService;
    private final ConcurrentHashMap<String, CachedKisSidebarQuote> kisSidebarQuoteCache =
            new ConcurrentHashMap<>();

    public SidebarWatchDto enrich(SidebarWatchDto dto) {
        boolean hasPrice = dto.getCurrentPrice() != null && dto.getCurrentPrice() > 0;
        boolean hasChangeRate =
                dto.getChangeRate() != null && dto.getChangeRate() != 0.0;

        if (hasPrice && hasChangeRate) {
            return dto;
        }

        SidebarWatchDto kisQuote = fetchKisQuote(dto.getStockCode(), dto.getStockName());

        if (hasPrice) {
            Double changeRate = resolveDailyChangeRate(dto.getChangeRate(), kisQuote);
            return SidebarWatchDto.builder()
                    .stockCode(dto.getStockCode())
                    .stockName(
                            kisQuote.getStockName() != null
                                    ? kisQuote.getStockName()
                                    : dto.getStockName())
                    .currentPrice(dto.getCurrentPrice())
                    .changeRate(changeRate)
                    .volume(dto.getVolume() != null ? dto.getVolume() : 0L)
                    .tradingValue(dto.getTradingValue() != null ? dto.getTradingValue() : 0L)
                    .build();
        }

        if (kisQuote.getCurrentPrice() == null || kisQuote.getCurrentPrice() <= 0) {
            return withZeroQuote(dto);
        }

        return SidebarWatchDto.builder()
                .stockCode(dto.getStockCode())
                .stockName(
                        kisQuote.getStockName() != null
                                ? kisQuote.getStockName()
                                : dto.getStockName())
                .currentPrice(kisQuote.getCurrentPrice())
                .changeRate(kisQuote.getChangeRate())
                .volume(kisQuote.getVolume())
                .tradingValue(kisQuote.getTradingValue())
                .build();
    }

    private SidebarWatchDto fetchKisQuote(String stockCode, String stockNameFallback) {
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
                stockName = stockNameFallback;
            }

            SidebarWatchDto enriched = SidebarWatchDto.builder()
                    .stockCode(stockCode)
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
            log.debug("KIS 시세 보완 실패 - stockCode={}", stockCode, e);
            return SidebarWatchDto.builder()
                    .stockCode(stockCode)
                    .stockName(stockNameFallback)
                    .currentPrice(0L)
                    .changeRate(0.0)
                    .volume(0L)
                    .tradingValue(0L)
                    .build();
        }
    }

    public AccountAssetResponse.HoldingStock enrichHolding(
            AccountAssetResponse.HoldingStock holding) {
        if (holding == null) {
            return null;
        }

        String stockCode = holding.getStockCode();
        SidebarWatchDto kisQuote = fetchKisQuote(stockCode, holding.getStockName());

        BigDecimal resolvedPrice = holding.getCurrentPrice();
        if (resolvedPrice == null || resolvedPrice.compareTo(BigDecimal.ZERO) <= 0) {
            long kisPrice =
                    kisQuote.getCurrentPrice() != null ? kisQuote.getCurrentPrice() : 0L;
            if (kisPrice > 0) {
                resolvedPrice = BigDecimal.valueOf(kisPrice);
            } else {
                resolvedPrice = BigDecimal.ZERO;
            }
        }

        long quantity = holding.getQuantity() != null ? holding.getQuantity() : 0L;
        BigDecimal stockValue = resolvedPrice
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(0, RoundingMode.HALF_UP);

        Double dailyChangeRate = resolveDailyChangeRate(holding.getDailyChangeRate(), kisQuote);

        String stockName = holding.getStockName();
        if (kisQuote.getStockName() != null && !kisQuote.getStockName().isBlank()) {
            stockName = kisQuote.getStockName();
        }

        return AccountAssetResponse.HoldingStock.builder()
                .stockCode(stockCode)
                .stockName(stockName)
                .quantity(holding.getQuantity())
                .avgPrice(holding.getAvgPrice())
                .currentPrice(resolvedPrice)
                .stockValue(stockValue)
                .dailyChangeRate(dailyChangeRate)
                .build();
    }

    private static Double resolveDailyChangeRate(
            Double dbRate,
            SidebarWatchDto kisQuote) {
        Double kisRate = kisQuote.getChangeRate();
        if (kisRate != null && kisRate != 0.0) {
            return kisRate;
        }
        if (dbRate != null) {
            return dbRate;
        }
        return kisRate != null ? kisRate : 0.0;
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
            String normalized = value
                    .replace(",", "")
                    .replace("%", "")
                    .trim();
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
