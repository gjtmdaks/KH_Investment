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
	private BigDecimal previousTotalAsset;
	private BigDecimal dailyProfitAmount;
	private BigDecimal dailyProfitRate;
	private BigDecimal availableCash;
	private BigDecimal stockValue;
	private BigDecimal baseCapital; // 초기 자본 10,000,000
	private BigDecimal baseProfitAmount;
	private BigDecimal baseProfitRate;
	private String accountStatus;
	private Date createdAt;
	
}