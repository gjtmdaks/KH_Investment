package com.kh.investSpring.domain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsAnalysisDto {

    private Long newsInfoId;

    private String sentiment;

    private Double score;

    private String aiSummary;
}