package com.kh.investSpring.domain.account.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kh.investSpring.domain.account.dto.AccountSummaryDto;
import com.kh.investSpring.domain.account.dto.HoldingDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

	@Override
	public AccountSummaryDto getAccount(Long userNo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<HoldingDto> getHoldings(Long userNo) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
