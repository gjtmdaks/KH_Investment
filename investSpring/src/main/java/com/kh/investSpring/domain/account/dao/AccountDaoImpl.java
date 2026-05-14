package com.kh.investSpring.domain.account.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.investSpring.domain.account.dto.AccountAssetResponse;
import com.kh.investSpring.domain.account.dto.AccountAssetSummaryDto;
import com.kh.investSpring.domain.account.dto.AccountSummaryDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
@RequiredArgsConstructor
public class AccountDaoImpl implements AccountDao {

	private final SqlSessionTemplate session;

	@Override
	public int updatePreviousTotalAssetForAllActiveAccounts() {
		// TODO Auto-generated method stub
		return session.update("account.updatePreviousTotalAssetForAllActiveAccounts");
	}
	
	@Override
	public int insertAccount(int userNo) {
		return session.insert("account.insertAccount", userNo);
		
	}
	
	@Override
	public int insertAccountBalance(int userNo) {
		return session.insert("account.insertAccountBalance", userNo);
		
	}
	
	@Override
	public int updateAccountStatusDeleteByUserNo(Long userNo) {
		return session.update("account.updateAccountStatusDeleteByUserNo", userNo);
		
	}
	
	@Override
	public AccountSummaryDto selectAccountSummaryByUserNo(Long userNo) {
		// TODO Auto-generated method stub
		return session.selectOne("account.selectAccountSummaryByUserNo", userNo);
	}

	@Override
	public AccountAssetSummaryDto selectAccountAssetByUserNo(Long userNo) {
	    return session.selectOne("account.selectAccountAssetByUserNo", userNo);
	}

	@Override
	public List<AccountAssetResponse.HoldingStock> selectHoldingStocksByUserNo(Long userNo) {
	    return session.selectList("account.selectHoldingStocksByUserNo", userNo);
	}
	
}
