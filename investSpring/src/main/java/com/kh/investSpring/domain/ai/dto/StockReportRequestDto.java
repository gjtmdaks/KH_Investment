package com.kh.investSpring.domain.ai.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class StockReportRequestDto {

    private String stockCode;

    private String stockName;

    private String sector;

    private String marketType;

    private Long issuedStock;

    private Long declinedStock;

    private Long treasuryStock;

    private Long outstandingShares;

    private Double minorityShareholderRatio;

    private Double minorityOwnershipRatio;

    private List<String> recentNews;
}