package com.kh.investSpring.domain.ai.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.kh.investSpring.domain.ai.dao.AiAnalysisDao;
import com.kh.investSpring.domain.ai.dto.NewsAnalysisDto;
import com.kh.investSpring.domain.ai.dto.NewsAnalysisTargetDto;
import com.kh.investSpring.domain.ai.dto.SentimentRequestDto;
import com.kh.investSpring.domain.ai.dto.SentimentResponseDto;
import com.kh.investSpring.domain.ai.dto.StockNewsAnalysisDto;
import com.kh.investSpring.domain.stock.dto.StockAiSummaryDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalysisServiceImpl implements AiAnalysisService {

    private final AiAnalysisDao aiAnalysisDao;
    private final WebClient webClient;
    @Value("${ai.base.url}")
    private String aiUrl;

    @Override
    public void analyzeNews() {
        List<NewsAnalysisTargetDto> targets = aiAnalysisDao.getUnAnalyzedNews();

        log.info("분석 대상 뉴스 개수={}", targets.size());

        for (NewsAnalysisTargetDto news : targets) {
            try {
                // Python 요청 DTO
                SentimentRequestDto request =
                        SentimentRequestDto.builder()
                                .title(news.getTitle())
                                .description(news.getDescription())
                                .build();

                // Python 호출
                SentimentResponseDto response =
                        webClient.post()
                                .uri(aiUrl+"/analysis/sentiment")
                                .bodyValue(request)
                                .retrieve()
                                .bodyToMono(SentimentResponseDto.class)
                                .block();

                if (response == null) {
                    log.warn("AI 응답 없음 newsId={}", news.getNewsInfoId());
                    continue;
                }

                log.info("AI 분석 완료 newsId={} sentiment={} score={}",
                        news.getNewsInfoId(),
                        response.getSentiment(),
                        response.getScore()
                );

                // DB 저장
                aiAnalysisDao.insertNewsAnalysis(
                        NewsAnalysisDto.builder()
                                .newsInfoId(news.getNewsInfoId())
                                .sentiment(response.getSentiment())
                                .score(response.getScore())
                                .aiSummary(null)
                                .build()
                );

            } catch (Exception e) {
                log.error("뉴스 분석 실패 newsId={}",
                        news.getNewsInfoId(),
                        e
                );
            }
        }
    }

    @Override
    public void analyzeStocks() {
        List<StockNewsAnalysisDto> rows = aiAnalysisDao.getStockNewsAnalysisTargets();

        log.info("종목 AI 분석 대상 rows={}", rows.size());

        Map<String, List<StockNewsAnalysisDto>> grouped =
                rows.stream()
                        .collect(Collectors.groupingBy(
                                StockNewsAnalysisDto::getStockCode
                        ));

        for (String stockCode : grouped.keySet()) {
            try {
                List<StockNewsAnalysisDto> newsList = grouped.get(stockCode);

                if (newsList == null || newsList.isEmpty()) {
                    continue;
                }

                int positive = 0;
                int negative = 0;
                int neutral = 0;
                
                double totalScore = 0;
                String stockName = newsList.get(0).getStockName();
                StringBuilder summaryBuilder = new StringBuilder();

                for (StockNewsAnalysisDto news : newsList) {
                    if ("POSITIVE".equals(news.getSentiment())) {
                        positive++;
                    } else if ("NEGATIVE".equals(news.getSentiment())) {
                        negative++;
                    } else {
                        neutral++;
                    }

                    totalScore += news.getScore();

                    summaryBuilder
                            .append(news.getTitle())
                            .append(", ");
                }

                double avgScore = totalScore / newsList.size();

                String finalSentiment;

                if (positive > negative) {
                    finalSentiment = "POSITIVE";
                } else if (negative > positive) {
                    finalSentiment = "NEGATIVE";
                } else {
                    finalSentiment = "NEUTRAL";
                }

                String summary;

                if ("POSITIVE".equals(finalSentiment)) {
                    summary = stockName + " 관련 긍정 뉴스 우세";
                } else if ("NEGATIVE".equals(finalSentiment)) {
                    summary = stockName + " 관련 악재 이슈 주의";
                } else {
                    summary = stockName + " 관련 혼조세";
                }

                aiAnalysisDao.mergeStockAiSummary(
                        StockAiSummaryDto.builder()
                                .stockCode(stockCode)
                                .sentiment(finalSentiment)
                                .summary(summary)
                                .score(avgScore)
                                .build()
                );

                log.info("종목 AI 저장 완료 stockCode={} sentiment={} score={}",
                        stockCode,
                        finalSentiment,
                        avgScore
                );

            } catch (Exception e) {
                log.error("종목 AI 분석 실패 stockCode={}",
                        stockCode,
                        e
                );
            }
        }
    }
}