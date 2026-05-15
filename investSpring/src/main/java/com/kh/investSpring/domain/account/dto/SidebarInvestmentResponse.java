package com.kh.investSpring.domain.account.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SidebarInvestmentResponse {

    private Account account;
    private List<Holding> holdings;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Account {
        private BigDecimal availableCash;   // 가용 가능 금액
        private BigDecimal investedAmount;  // 내가 투자한 금액
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Holding {
        private String stockCode;           // 종목코드
        private String stockName;           // 종목명
        private Long quantity;              // 보유 수량
        private BigDecimal avgPrice;        // 평균단가
        private BigDecimal currentPrice;    // 현재가
        private BigDecimal stockValue;      // 평가금액
        private BigDecimal profitAmount;    // 평가손익
        private BigDecimal profitRate;      // 수익률
    }
}