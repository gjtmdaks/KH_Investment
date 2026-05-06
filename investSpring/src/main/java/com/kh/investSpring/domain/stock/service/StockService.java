package com.kh.investSpring.domain.stock.service;

import java.util.List;

import com.kh.investSpring.domain.stock.dto.StockDto;
import com.kh.investSpring.domain.stock.dto.TopStockDto;

public interface StockService {

    // ✅ 메인 종목 리스트 (실시간 반영 대상)
    List<StockDto> getStockList();

    // ✅ 거래대금 1위 종목
    TopStockDto getTopVolumeStock();
}