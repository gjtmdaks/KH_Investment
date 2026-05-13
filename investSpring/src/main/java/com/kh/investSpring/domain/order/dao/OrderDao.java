package com.kh.investSpring.domain.order.dao;

import java.math.BigDecimal;

import com.kh.investSpring.domain.order.dto.OrderRequest;

public interface OrderDao {
	
	Long selectActiveAccountNoByUserNo(Long userNo);

	BigDecimal selectAvailableCashByAccountNo(Long accountNo);

	Long selectHoldingQuantityByAccountNoAndStockCode(Long accountNo, String stockCode);

	int insertOrder(Long userNo, Long accountNo, OrderRequest request, String status);

	int updateAccountBalanceForBuy(Long accountNo, BigDecimal orderAmount);

	int updateAccountBalanceForSell(Long accountNo, BigDecimal orderAmount);

	int updateHoldingForBuy(Long accountNo, String stockCode, Long quantity, BigDecimal price);

	int updateHoldingForSell(Long accountNo, String stockCode, Long quantity);
}	
