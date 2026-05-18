package com.kh.investSpring.domain.stock.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TopStockDto {

    private String stockCode;
    private String stockName;
    private Long price;
    private Double changeRate;

    // 📈 미니 차트 (최근 n틱)
    private List<Long> miniChart;

}