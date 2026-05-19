package com.kh.investSpring.domain.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAiSummaryDto {

    private String stockCode;

    private String sentiment;

    private String summary;

    private Double score;
}