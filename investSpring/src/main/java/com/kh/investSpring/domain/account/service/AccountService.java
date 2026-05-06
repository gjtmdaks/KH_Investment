package com.kh.investSpring.domain.account.service;

import java.util.List;

import com.kh.investSpring.domain.account.dto.AccountSummaryDto;
import com.kh.investSpring.domain.account.dto.HoldingDto;

public interface AccountService {

    // ✅ 계좌 요약 (잔액)
    AccountSummaryDto getAccount(Long userNo);

    // ✅ 보유 주식
    List<HoldingDto> getHoldings(Long userNo);
}