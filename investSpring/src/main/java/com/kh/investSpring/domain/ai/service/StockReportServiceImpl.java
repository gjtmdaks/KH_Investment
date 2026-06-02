package com.kh.investSpring.domain.ai.service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.kh.investSpring.domain.ai.dao.StockReportDao;
import com.kh.investSpring.domain.ai.dto.StockAiReportDto;
import com.kh.investSpring.domain.ai.dto.StockReportRequestDto;
import com.kh.investSpring.domain.ai.dto.StockReportResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockReportServiceImpl implements StockReportService {

    private static final long STOCK_REPORT_CACHE_TTL_MS = 60_000L;

    private final StockReportDao stockReportDao;
    private final RestTemplate restTemplate;
    private final ConcurrentHashMap<String, CachedStockReport> stockReportCache =
            new ConcurrentHashMap<>();

    @Value("${ai.base.url}")
    private String aiUrl;

    @Override
    public void generateReports() {
        List<StockReportRequestDto> targets = stockReportDao.getReportTargets();

        if (targets.isEmpty()) {
            log.info("AI 리포트 대상 종목 없음");
            return;
        }
        
        for (StockReportRequestDto stock : targets) {
            try {
                List<String> recentNews = stockReportDao.getRecentNews(stock.getStockCode());

                stock.setRecentNews(recentNews);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setConnection("close");

                HttpEntity<StockReportRequestDto> entity =
                        new HttpEntity<>(stock, headers);

                StockReportResponseDto response =
                        restTemplate.postForObject(
                        		aiUrl+"/analysis/stock-report",
                        		entity,
                                StockReportResponseDto.class
                        );

                if (response == null) {
                    continue;
                }

                StockAiReportDto report = StockAiReportDto.builder()
                        .stockCode(stock.getStockCode())
                        .investmentOpinion(response.getInvestmentOpinion())
                        .confidenceScore(response.getConfidenceScore())
                        .summary(response.getSummary())
                        .riskFactors(response.getRiskFactors())
                        .positiveFactors(response.getPositiveFactors())
                        .aiSignal(response.getAiSignal())
                        .build();

                stockReportDao.upsertStockReport(report);

            } catch (Exception e) {
                log.error("AI 리포트 생성 실패 stockCode={}",
                        stock.getStockCode(),
                        e
                );
            }
        }
    }
    
    @Override
    public StockAiReportDto getStockReport(String stockCode) {
        String code = stockCode == null ? "" : stockCode.trim();
        long now = System.currentTimeMillis();
        CachedStockReport cached = stockReportCache.get(code);

        if (cached != null && cached.expiresAtMs() > now) {
            return cached.report();
        }

        StockAiReportDto report = stockReportDao.selectStockReport(stockCode);
        stockReportCache.put(
                code,
                new CachedStockReport(report, now + STOCK_REPORT_CACHE_TTL_MS)
        );

        return report;
    }

    private record CachedStockReport(
            StockAiReportDto report,
            long expiresAtMs
    ) {
    }
}