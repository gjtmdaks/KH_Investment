package com.kh.investSpring.domain.ai.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kh.investSpring.domain.ai.service.AiAnalysisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsAnalysisScheduler {

    private final AiAnalysisService aiAnalysisService;

    @Scheduled(fixedDelay = 100000)
    public void analyzeNews() {
        log.info("뉴스 AI 분석 스케줄 시작");

        try {
            aiAnalysisService.analyzeNews();

        } catch (Exception e) {
            log.error("뉴스 AI 분석 실패", e);
        }
    }
    
    @Scheduled(fixedDelay = 100000)
    public void analyzeStocks() {
    	log.info("종목 AI 분석 스케줄 시작");
    	
    	try {
    		aiAnalysisService.analyzeStocks();
    		
    	} catch (Exception e) {
    		log.error("종목 AI 분석 실패", e);
    	}
    }
}