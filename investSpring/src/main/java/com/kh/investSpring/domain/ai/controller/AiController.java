package com.kh.investSpring.domain.ai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.investSpring.domain.ai.dto.StockAiReportDto;
import com.kh.investSpring.domain.ai.dto.UserAiProfileDto;
import com.kh.investSpring.domain.ai.service.StockReportService;
import com.kh.investSpring.domain.ai.service.UserAiProfileService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final StockReportService stockReportService;
    private final UserAiProfileService userAiProfileService;

    @GetMapping("/stock-report/{stockCode}")
    public ResponseEntity<StockAiReportDto> getStockReport(@PathVariable String stockCode) {
        StockAiReportDto report = stockReportService.getStockReport(stockCode);

        if (report == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/user-profile")
    public ResponseEntity<UserAiProfileDto> getUserProfile(HttpServletRequest request) {
        Long userNo = (Long)request.getAttribute("userNo");

        UserAiProfileDto profile = userAiProfileService.getUserProfile(userNo);

        if (profile == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(profile);
    }
    
    @PostMapping("/user-profile/analyze")
    public ResponseEntity<Void> analyzeUserProfile(HttpServletRequest request) {
        Long userNo = (Long)request.getAttribute("userNo");

        userAiProfileService.analyzeUserProfile(userNo);

        return ResponseEntity.ok().build();
    }
}