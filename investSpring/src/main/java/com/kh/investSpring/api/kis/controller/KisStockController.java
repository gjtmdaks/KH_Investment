package com.kh.investSpring.api.kis.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.investSpring.api.kis.dto.KisStockCandleResponse;
import com.kh.investSpring.api.kis.dto.KisStockDetailResponse;
import com.kh.investSpring.api.kis.dto.KisStockOrderbookResponse;
import com.kh.investSpring.api.kis.dto.KisStockPriceResponse;
import com.kh.investSpring.api.kis.dto.KisStockSummaryResponse;
import com.kh.investSpring.api.kis.dto.StockBatchPriceRequest;
import com.kh.investSpring.api.kis.service.KisStockService;
import com.kh.investSpring.api.kis.service.StockHistoryReadService;

@RestController
public class KisStockController {

    private final KisStockService kisStockService;
    private final StockHistoryReadService stockHistoryReadService;

    public KisStockController(
            KisStockService kisStockService,
            StockHistoryReadService stockHistoryReadService
    ) {
        this.kisStockService = kisStockService;
        this.stockHistoryReadService = stockHistoryReadService;
    }

    @GetMapping("/api/stocks/{stockCode}/price")
    public KisStockPriceResponse getStockPrice(@PathVariable String stockCode) {
        return kisStockService.getStockPrice(stockCode);
    }

    @PostMapping("/api/stocks/prices/batch")
    public Map<String, String> getBatchChangeRates(@RequestBody(required = false) StockBatchPriceRequest request) {
        List<String> codes = request != null ? request.stockCodes() : null;
        return kisStockService.getChangeRatesByStockCodes(codes != null ? codes : List.of());
    }

    @GetMapping("/api/stocks/{stockCode}/orderbook")
    public KisStockOrderbookResponse getStockOrderbook(@PathVariable String stockCode) {
        return kisStockService.getStockOrderbook(stockCode);
    }

    @GetMapping("/api/stocks/{stockCode}/summary")
    public KisStockSummaryResponse getStockSummary(@PathVariable String stockCode) {
        return kisStockService.getStockSummary(stockCode);
    }

    @GetMapping("/api/stocks/{stockCode}/detail")
    public KisStockDetailResponse getStockDetail(@PathVariable String stockCode) {
        return kisStockService.getStockDetail(stockCode);
    }

    @GetMapping("/api/stocks/{stockCode}/candles")
    public KisStockCandleResponse getStockCandles(
            @PathVariable String stockCode,
            @RequestParam String period,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return stockHistoryReadService.getCandles(stockCode, period, from, to);
    }
}
