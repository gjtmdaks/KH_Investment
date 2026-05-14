package com.kh.investSpring.domain.account.dao;

import java.util.List;

import com.kh.investSpring.domain.account.dto.AccountAssetResponse.HoldingStock;
import com.kh.investSpring.domain.account.dto.AccountAssetSummaryDto;
import com.kh.investSpring.domain.account.dto.AccountSummaryDto;

public interface AccountDao {
	
	int updatePreviousTotalAssetForAllActiveAccounts();
	
	int insertAccount(int userNo);
	
	int insertAccountBalance(int userNo);
	
	int updateAccountStatusDeleteByUserNo(Long userNo);
	
	AccountSummaryDto selectAccountSummaryByUserNo(Long userNo);

	AccountAssetSummaryDto selectAccountAssetByUserNo(Long userNo);

	List<HoldingStock> selectHoldingStocksByUserNo(Long userNo);

}
