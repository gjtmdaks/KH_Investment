package com.kh.investSpring.domain.ai.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserProfileAnalyzeResponseDto {

    private String surveyType;

    private String actualInvestmentType;

    private List<String> portfolioRiskAnalysis;

    private List<String> aiRecommendations;
}