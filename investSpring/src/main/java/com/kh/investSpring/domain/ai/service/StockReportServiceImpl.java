package com.kh.investSpring.domain.ai.service;

import java.util.List;

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

    private final StockReportDao stockReportDao;
    private final RestTemplate restTemplate;
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
        return stockReportDao.selectStockReport(stockCode);
    }
}