package com.kh.investSpring.domain.ai.dto;

import java.util.Date;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockAiReportDto {

    private String stockCode;

    private String investmentOpinion;

    private Integer confidenceScore;

    private String summary;

    private String riskFactors;

    private String positiveFactors;

    private String aiSignal;

    private Date updatedAt;
}