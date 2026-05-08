package com.kh.investSpring.api.dart.dto;

import lombok.Data;

@Data
public class StockTotalQuantityDto {

    private String corpCode;
    private String stockCode;

    // 현재까지 발행한 주식의 총수
    private Long issuedStock;

    // 현재까지 감소한 주식의 총수
    private Long declinedStock;

    // 자기주식수
    private Long treasuryStock;

    // 유통주식수
    private Long outstandingShares;
}