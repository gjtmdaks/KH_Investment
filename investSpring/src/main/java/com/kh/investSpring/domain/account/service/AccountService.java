package com.kh.investSpring.domain.account.service;

import java.util.List;

import com.kh.investSpring.domain.account.dto.AccountSummaryDto;
import com.kh.investSpring.domain.account.dto.HoldingDto;

public interface AccountService {

	AccountSummaryDto getAccountSummary(Long userNo);
}