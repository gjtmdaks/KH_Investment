package com.kh.investSpring.domain.account.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountAssetResponse {

    private BigDecimal totalAsset;       // 총 자산

    private BigDecimal availableCash;    // 가용 현금

    private BigDecimal lockedCash;       // 주문 묶인 돈

    private BigDecimal totalStockValue;  // 보유 주식 평가액

    private List<HoldingStock> holdings; // 보유 주식 목록

    @Getter
    @Builder
    public static class HoldingStock {

        private String stockCode;        // 종목코드

        private String stockName;        // 종목명

        private Long quantity;           // 보유수량

        private BigDecimal avgPrice;     // 평균단가

        private BigDecimal currentPrice; // 현재가

        private BigDecimal stockValue;   // 평가금액
    }
}
