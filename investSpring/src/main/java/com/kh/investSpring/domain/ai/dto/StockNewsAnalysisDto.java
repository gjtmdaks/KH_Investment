package com.kh.investSpring.domain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockNewsAnalysisDto {

    private String stockCode;
    private String stockName;

    private Long newsInfoId;

    private String title;
    private String description;

    private String sentiment;
    private Double score;
}