package com.kh.investSpring.api.kis.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.stereotype.Service;

import com.kh.investSpring.api.kis.dto.StockRealtimeTickDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RealtimeQueueServiceImpl implements RealtimeQueueService {

    private final Queue<StockRealtimeTickDto> queue = new ConcurrentLinkedQueue<>();

    public void add(StockRealtimeTickDto dto) {
        queue.offer(dto);
    }

    public List<StockRealtimeTickDto> pollBatch(int size) {

        List<StockRealtimeTickDto> list = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            StockRealtimeTickDto dto = queue.poll();

            if (dto == null) {
            	log.info("Queue Empty");
                break;
            }

            list.add(dto);
        }

        return list;
    }
}