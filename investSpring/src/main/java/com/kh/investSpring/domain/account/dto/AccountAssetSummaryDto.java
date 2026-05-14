package com.kh.investSpring.domain.account.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AccountAssetSummaryDto {

    private BigDecimal totalAsset;

    private BigDecimal availableCash;

    private BigDecimal lockedCash;

    private BigDecimal totalStockValue;
}