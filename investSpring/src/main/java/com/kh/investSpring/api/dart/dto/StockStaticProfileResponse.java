package com.kh.investSpring.api.dart.dto;

public record StockStaticProfileResponse(
        String stockCode,
        String stockName,
        String marketType,
        String sector,
        String listedDate,
        String status,
        String corpCode,
        String coName,
        String issuedStock,
        String declinedStock,
        String treasuryStock,
        String outstandingShares,
        String shareholdingRatio,
        String ownershipPercentage) {
}
