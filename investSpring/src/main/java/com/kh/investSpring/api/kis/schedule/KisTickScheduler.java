package com.kh.investSpring.api.kis.schedule;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kh.investSpring.api.kis.config.KisProperties;
import com.kh.investSpring.api.kis.dao.StockRealtimeDao;
import com.kh.investSpring.api.kis.dto.StockRealtimeTickDto;
import com.kh.investSpring.api.kis.service.RealtimeQueueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisTickScheduler {

    private final KisProperties kisProperties;
    private final RealtimeQueueService queueService;
    private final StockRealtimeDao stockRealtimeDao;
    
    /**
     * 매 10시마다 실시간 시세 데이터 비우기
     */
    @Scheduled(cron = "0 0 10 * * *")
    public void cleanupRealtimeTick() {

        stockRealtimeDao.deleteOldTicks();

        log.info("실시간 tick 데이터 정리 완료");
    }

    /**
     * 1초마다 batch insert
     */
    @Scheduled(fixedRate = 1000)
    public void saveTickData() {
        if (!kisProperties.isWebsocketEnabled()) {
            return;
        }
        List<StockRealtimeTickDto> batch = queueService.pollBatch(1000);

        if (!batch.isEmpty()) {
            log.info("tick save batch={}", batch.size());
        }

        if (batch.isEmpty()) return;

        stockRealtimeDao.batchInsertTick(batch);

        log.info("실시간 Tick 저장 완료: {}건", batch.size());
    }
    
    /**
     * 0.5초마다 batch insert
     */
    @Scheduled(fixedRate = 500)
    public void saveCurrentData() {

        List<StockRealtimeTickDto> batch = queueService.pollCurrentBatch();

        if (batch.isEmpty()) {
            return;
        }

        for (StockRealtimeTickDto dto : batch) {
        	int updated = stockRealtimeDao.updateRealtimeCurrent(dto);

        		if (updated == 0) {
        		    try {
        		        stockRealtimeDao.insertRealtimeCurrent(dto);
        		    } catch (DuplicateKeyException ignored) {
        		    }
        		}
        }

        log.info("current 갱신 완료={}", batch.size());
    }
}