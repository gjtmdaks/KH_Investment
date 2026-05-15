package com.kh.investSpring.domain.account.dao;

import java.util.List;

import com.kh.investSpring.domain.account.dto.AccountAssetResponse.HoldingStock;
import com.kh.investSpring.domain.account.dto.AccountAssetSummaryDto;
import com.kh.investSpring.domain.account.dto.AccountSummaryDto;
import com.kh.investSpring.domain.main.dto.MainResponse.Account;
import com.kh.investSpring.domain.main.dto.MainResponse.Holding;

public interface AccountDao {
	
	int updatePreviousTotalAssetForAllActiveAccounts();
	
	int insertAccount(int userNo);
	
	int insertAccountBalance(int userNo);
	
	int updateAccountStatusDeleteByUserNo(Long userNo);
	
	AccountSummaryDto selectAccountSummaryByUserNo(Long userNo);

	AccountAssetSummaryDto selectAccountAssetByUserNo(Long userNo);

	List<HoldingStock> selectHoldingStocksByUserNo(Long userNo);

	Account selectSidebarAccountByUserNo(Long userNo);

	List<Holding> selectSidebarHoldingsByUserNo(Long userNo);

}
