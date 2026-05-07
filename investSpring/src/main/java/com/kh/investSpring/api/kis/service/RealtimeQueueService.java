package com.kh.investSpring.api.kis.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kh.investSpring.api.kis.dto.StockRealtimeTickDto;

@Service
public interface RealtimeQueueService {

    public void add(StockRealtimeTickDto dto);

    public List<StockRealtimeTickDto> pollBatch(int size);
}