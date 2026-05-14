package com.kh.investSpring.domain.order.service;

import java.util.List;

import com.kh.investSpring.domain.order.dto.OrderHistoryResponse;
import com.kh.investSpring.domain.order.dto.TradeResponse;

public interface OrderQueryService {

	List<TradeResponse> getTradeHistory(Long userNo);

	List<OrderHistoryResponse> getOrderHistory(Long userNo);

}
