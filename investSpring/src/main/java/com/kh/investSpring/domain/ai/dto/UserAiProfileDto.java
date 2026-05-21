package com.kh.investSpring.domain.ai.dto;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class UserAiProfileDto {

    private Long userNo;

    private String surveyType;

    private String actualInvestmentType;

    private List<String> portfolioRiskAnalysis;

    private List<String> aiRecommendations;

    // DB 저장용
    private String portfolioRiskAnalysisJson;

    private String aiRecommendationsJson;

    private Date createdAt;

    private Date updatedAt;
}