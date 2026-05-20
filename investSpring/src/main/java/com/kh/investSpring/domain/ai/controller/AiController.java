package com.kh.investSpring.domain.ai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.investSpring.domain.ai.dto.StockAiReportDto;
import com.kh.investSpring.domain.ai.service.StockReportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final StockReportService stockReportService;

    @GetMapping("/stock-report/{stockCode}")
    public ResponseEntity<StockAiReportDto> getStockReport(@PathVariable String stockCode) {
        StockAiReportDto report = stockReportService.getStockReport(stockCode);

        if (report == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(report);
    }
}