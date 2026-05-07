package com.kh.investSpring.api.kis.schedule;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kh.investSpring.api.kis.dao.StockRealtimeDao;
import com.kh.investSpring.api.kis.dto.StockRealtimeTickDto;
import com.kh.investSpring.api.kis.service.RealtimeQueueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisTickScheduler {

    private final RealtimeQueueService queueService;
    private final StockRealtimeDao stockRealtimeDao;

    /**
     * 1초마다 batch insert
     */
    @Scheduled(fixedRate = 1000)
    public void saveTickData() {

        List<StockRealtimeTickDto> batch = queueService.pollBatch(1000);

        if (batch.isEmpty()) return;
        
        for (StockRealtimeTickDto dto : batch) {
            log.info(
                "DB 저장 시도 stockCode={}",
                dto.getStockCode()
            );
        }

        stockRealtimeDao.batchInsertTick(batch);

        log.info("실시간 Tick 저장 완료: {}건", batch.size());
    }
}