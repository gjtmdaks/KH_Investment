package com.kh.investSpring.domain.ai.scheduler;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kh.investSpring.domain.ai.service.StockReportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockReportScheduler {

    private final StockReportService stockReportService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * GPT 기반 종목 상세 AI 리포트 생성
     */
    //@Scheduled(fixedDelay = 120000, scheduler = "aiScheduler")
    public void generateStockReports() {
        if (!running.compareAndSet(false, true)) {
            log.warn("이미 실행 중이라 스킵");
            return;
        }
        
        log.info("GPT 종목 AI 리포트 스케줄 시작");
        try {
            stockReportService.generateReports();

        } catch (Exception e) {
            log.error("GPT 종목 AI 리포트 스케줄 실패", e);
        } finally {
            running.set(false);
        }
    }
}