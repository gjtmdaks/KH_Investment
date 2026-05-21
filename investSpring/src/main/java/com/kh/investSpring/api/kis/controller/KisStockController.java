package com.kh.investSpring.api.kis.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.investSpring.api.kis.dto.KisStockCandleResponse;
import com.kh.investSpring.api.kis.dto.KisStockDetailResponse;
import com.kh.investSpring.api.kis.dto.OrderbookSubscribeResponse;
import com.kh.investSpring.api.kis.dto.StockOrderbookViewResponse;
import com.kh.investSpring.api.kis.dto.StockPriceViewResponse;
import com.kh.investSpring.api.kis.dto.KisStockSummaryResponse;
import com.kh.investSpring.api.kis.dto.StockBatchPriceRequest;
import com.kh.investSpring.api.kis.dto.StockInvestorTrendResponse;
import com.kh.investSpring.api.kis.service.KisInvestorTradeService;
import com.kh.investSpring.api.kis.service.KisStockService;
import com.kh.investSpring.api.kis.service.StockHistoryReadService;
import com.kh.investSpring.api.kis.service.StockMinuteReadService;

@RestController
public class KisStockController {

    private final KisStockService kisStockService;
    private final KisInvestorTradeService kisInvestorTradeService;
    private final StockHistoryReadService stockHistoryReadService;
    private final StockMinuteReadService stockMinuteReadService;

    public KisStockController(
            KisStockService kisStockService,
            KisInvestorTradeService kisInvestorTradeService,
            StockHistoryReadService stockHistoryReadService,
            StockMinuteReadService stockMinuteReadService
    ) {
        this.kisStockService = kisStockService;
        this.kisInvestorTradeService = kisInvestorTradeService;
        this.stockHistoryReadService = stockHistoryReadService;
        this.stockMinuteReadService = stockMinuteReadService;
    }

    @GetMapping("/api/stocks/{stockCode}/price")
    public StockPriceViewResponse getStockPrice(@PathVariable String stockCode) {
        return kisStockService.getStockPriceView(stockCode);
    }

    @PostMapping("/api/stocks/prices/batch")
    public Map<String, String> getBatchChangeRates(@RequestBody(required = false) StockBatchPriceRequest request) {
        List<String> codes = request != null ? request.stockCodes() : null;
        return kisStockService.getChangeRatesByStockCodes(codes != null ? codes : List.of());
    }

    @GetMapping("/api/stocks/{stockCode}/orderbook")
    public StockOrderbookViewResponse getStockOrderbook(@PathVariable String stockCode) {
        return kisStockService.getStockOrderbookView(stockCode);
    }

    @PostMapping("/api/stocks/{stockCode}/orderbook/subscribe")
    public OrderbookSubscribeResponse subscribeOrderbook(@PathVariable String stockCode) {
        boolean subscribed = kisStockService.subscribeOrderbookDemand(stockCode);
        return new OrderbookSubscribeResponse(subscribed);
    }

    @DeleteMapping("/api/stocks/{stockCode}/orderbook/subscribe")
    public void unsubscribeOrderbook(@PathVariable String stockCode) {
        kisStockService.unsubscribeOrderbookDemand(stockCode);
    }

    @GetMapping("/api/stocks/{stockCode}/summary")
    public KisStockSummaryResponse getStockSummary(@PathVariable String stockCode) {
        return kisStockService.getStockSummary(stockCode);
    }

    @GetMapping("/api/stocks/{stockCode}/detail")
    public KisStockDetailResponse getStockDetail(@PathVariable String stockCode) {
        return kisStockService.getStockDetail(stockCode);
    }

    @GetMapping("/api/stocks/{stockCode}/investor-trend")
    public StockInvestorTrendResponse getInvestorTrend(
            @PathVariable String stockCode,
            @RequestParam(required = false, defaultValue = "30") int days) {
        return kisInvestorTradeService.getInvestorTrend(stockCode, days);
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

    @GetMapping("/api/stocks/{stockCode}/minute-candles")
    public KisStockCandleResponse getMinuteCandles(
            @PathVariable String stockCode,
            @RequestParam int intervalMinutes,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tradeDate
    ) {
        return stockMinuteReadService.getMinuteCandles(stockCode, intervalMinutes, tradeDate);
    }
}
