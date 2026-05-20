package com.kh.investSpring.domain.ai.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class StockReportResponseDto {

    private String investmentOpinion;

    private Integer confidenceScore;

    private String summary;

    private String riskFactors;

    private String positiveFactors;

    private String aiSignal;
}