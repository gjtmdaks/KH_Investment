package com.kh.investSpring.domain.account.service;


import java.util.List;

import com.kh.investSpring.domain.account.dto.AccountAssetResponse;
import com.kh.investSpring.domain.account.dto.AccountSummaryDto;
import com.kh.investSpring.domain.main.dto.MainResponse;

public interface AccountService {

	AccountSummaryDto getAccountSummary(Long userNo);

	int updatePreviousTotalAssetForAllActiveAccounts();

	AccountAssetResponse getAccountAssets(Long userNo);
	
	MainResponse.Account getSidebarAccount(Long userNo);

	List<MainResponse.Holding> getSidebarHoldings(Long userNo);
}