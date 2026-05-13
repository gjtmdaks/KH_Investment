package com.kh.investSpring.domain.account.dao;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.investSpring.domain.account.dto.AccountSummaryDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
@RequiredArgsConstructor
public class AccountDaoImpl implements AccountDao {

	private final SqlSessionTemplate session;
	
	
	
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
	
}
