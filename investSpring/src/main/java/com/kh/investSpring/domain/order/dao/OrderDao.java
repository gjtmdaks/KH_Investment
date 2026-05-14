package com.kh.investSpring.domain.order.dao;

import java.math.BigDecimal;
import java.util.List;

import com.kh.investSpring.domain.order.dto.OrderRequest;
import com.kh.investSpring.domain.order.dto.TradeResponse;

public interface OrderDao {
	
	Long selectActiveAccountNoByUserNo(Long userNo);

	BigDecimal selectAvailableCashByAccountNo(Long accountNo);

	Long selectHoldingQuantityByAccountNoAndStockCode(Long accountNo, String stockCode);

	int insertOrder(Long userNo, Long accountNo, OrderRequest request, String status);

	int updateAccountBalanceForBuy(Long accountNo, BigDecimal orderAmount);

	int updateAccountBalanceForSell(Long accountNo, BigDecimal orderAmount);

	int updateHoldingForBuy(Long accountNo, String stockCode, Long quantity, BigDecimal price);

	int updateHoldingForSell(Long accountNo, String stockCode, Long quantity);
	
	Long selectNextOrderId();
	int insertTrade(Long orderId, BigDecimal price, Long quantity);
	int insertOrder(Long orderId, Long userNo, Long accountNo, OrderRequest request, String status);

	List<TradeResponse> selectTradeHistoryByUserNo(Long userNo);

	int updateAccountBalanceForPendingBuy(Long accountNo, BigDecimal orderAmount);

	Long selectSellableQuantityByAccountNoAndStockCode(Long accountNo, String stockCode);
}	
