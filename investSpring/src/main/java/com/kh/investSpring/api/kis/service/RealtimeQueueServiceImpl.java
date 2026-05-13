package com.kh.investSpring.api.kis.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.stereotype.Service;

import com.kh.investSpring.api.kis.dto.StockRealtimeTickDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RealtimeQueueServiceImpl implements RealtimeQueueService {

    private final Queue<StockRealtimeTickDto> queue = new ConcurrentLinkedQueue<>();
    private final Map<String, StockRealtimeTickDto> currentMap = new ConcurrentHashMap<>();

    public void add(StockRealtimeTickDto dto) {
    	// tick 저장용
        queue.offer(dto);
        
        // current 최신 상태용
        currentMap.put(
            dto.getStockCode(),
            dto
        );
    }

    @Override
    public List<StockRealtimeTickDto> pollBatch(int size) {
        List<StockRealtimeTickDto> list = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            StockRealtimeTickDto dto = queue.poll();

            if (dto == null) {
                break;
            }
            list.add(dto);
        }

        return list;
    }
    
    @Override
    public List<StockRealtimeTickDto> pollCurrentBatch() {
        List<StockRealtimeTickDto> list = new ArrayList<>(currentMap.values());

        currentMap.clear();

        return list;
    }

    @Override
    public int size() {
        return queue.size();
    }
}