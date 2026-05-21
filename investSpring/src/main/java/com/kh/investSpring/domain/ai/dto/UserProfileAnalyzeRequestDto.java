package com.kh.investSpring.domain.ai.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserProfileAnalyzeRequestDto {

    private String surveyResult;

    private List<SurveyAnswerDto> surveyAnswers;

    private PortfolioDto portfolio;

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SurveyAnswerDto {

        private String question;

        private String answer;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PortfolioDto {

        private BigDecimal totalAsset;

        private Double cashRatio;

        private Integer stockCount;

        private List<SectorAllocationDto> sectorAllocation;

        private Map<String, Integer> marketAllocation;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SectorAllocationDto {

        private String sector;

        private Integer ratio;
    }
}