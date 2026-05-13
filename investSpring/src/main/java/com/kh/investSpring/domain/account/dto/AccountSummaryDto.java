package com.kh.investSpring.domain.account.dto;

import java.math.BigDecimal;
import java.util.Date;

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
public class AccountSummaryDto {
    
    private BigDecimal currentTotalAsset;
    private BigDecimal profitAmount;
    private BigDecimal profitRate;
    private BigDecimal availableCash;
    private BigDecimal stockValue;
    private BigDecimal initialBalance;
    private BigDecimal initialProfitRate;
    private String accountStatus;
    private Date createdAt;
	
}