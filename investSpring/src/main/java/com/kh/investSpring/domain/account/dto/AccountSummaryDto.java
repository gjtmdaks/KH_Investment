package com.kh.investSpring.domain.account.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountSummaryDto {

    private Long balance;
}