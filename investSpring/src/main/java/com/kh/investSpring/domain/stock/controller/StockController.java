package com.kh.investSpring.domain.stock.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.investSpring.domain.stock.dto.RealtimeSectionResponseDto;
import com.kh.investSpring.domain.stock.dto.StockScreenerDto;
import com.kh.investSpring.domain.stock.service.StockService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping("/screener/rising")
    public List<StockScreenerDto> getRisingStocks() {
        return stockService.getRisingStocks();
    }

    @GetMapping("/screener/falling")
    public List<StockScreenerDto> getFallingStocks() {
        return stockService.getFallingStocks();
    }

    @GetMapping("/screener/watchlist")
    public List<StockScreenerDto> getWatchlistStocks() {
        return stockService.getPopularWatchlistStocks();
    }

    @GetMapping("/screener/viewed")
    public List<StockScreenerDto> getViewedStocks() {
        return stockService.getViewedStocks();
    }

    @GetMapping("/screener/volume")
    public List<StockScreenerDto> getVolumeStocks() {
        return stockService.getVolumeStocks();
    }
    
    @GetMapping("/screener/search")
    public List<StockScreenerDto> searchStocks(
            @RequestParam(required = false) String market,
            @RequestParam(required = false) String changeRate,
            @RequestParam(required = false) String volume
    ) {
        return stockService.searchStocks(market, changeRate, volume);
    }
    
    @GetMapping("/screener/realtime")
    public RealtimeSectionResponseDto realtimeStocks() {
    	return stockService.getRealtimeSection();
    }
}