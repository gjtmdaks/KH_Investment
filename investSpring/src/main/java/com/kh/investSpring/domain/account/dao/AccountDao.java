package com.kh.investSpring.domain.account.dao;

import com.kh.investSpring.domain.account.dto.AccountSummaryDto;

public interface AccountDao {
	
	int updatePreviousTotalAssetForAllActiveAccounts();
	
	int insertAccount(int userNo);
	
	int insertAccountBalance(int userNo);
	
	int updateAccountStatusDeleteByUserNo(Long userNo);
	
	AccountSummaryDto selectAccountSummaryByUserNo(Long userNo);

}
