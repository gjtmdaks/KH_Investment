package com.kh.investSpring.api.kis;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.kh.investSpring.api.dto.response.KisStockPriceResponse;

@RestController
public class KisStockController {

    private final KisStockService kisStockService;

    public KisStockController(KisStockService kisStockService) {
        this.kisStockService = kisStockService;
    }

    @GetMapping("/api/stocks/{stockCode}/price")
    public KisStockPriceResponse getStockPrice(@PathVariable String stockCode) {
        return kisStockService.getStockPrice(stockCode);
    }
}
