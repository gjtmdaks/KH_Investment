package com.kh.investSpring.domain.account.service;


import com.kh.investSpring.domain.account.dto.AccountAssetResponse;
import com.kh.investSpring.domain.account.dto.AccountSummaryDto;

public interface AccountService {

	AccountSummaryDto getAccountSummary(Long userNo);

	int updatePreviousTotalAssetForAllActiveAccounts();

	AccountAssetResponse getAccountAssets(Long userNo);
}