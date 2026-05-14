package com.kh.investSpring.domain.order.service;

import com.kh.investSpring.domain.order.dto.OrderPriceUpdateRequest;
import com.kh.investSpring.domain.order.dto.OrderRequest;
import com.kh.investSpring.domain.order.dto.OrderResponse;

public interface OrderCommandService {

	OrderResponse createOrder(Long userNo, OrderRequest request);

	void cancelOrder(Long userNo, Long orderId);

	void updateOrderPrice(Long userNo, Long orderId, OrderPriceUpdateRequest request);

}
