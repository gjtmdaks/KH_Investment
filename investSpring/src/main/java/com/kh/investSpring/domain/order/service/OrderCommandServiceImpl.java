package com.kh.investSpring.domain.order.service;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.investSpring.domain.order.dao.OrderDao;
import com.kh.investSpring.domain.order.dto.OrderRequest;
import com.kh.investSpring.domain.order.dto.OrderResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderCommandServiceImpl implements OrderCommandService {

    private final OrderDao orderDao;

    @Override
    @Transactional
    public OrderResponse createOrder(Long userNo, OrderRequest request) {
        validateOrderRequest(userNo, request);

        if ("BUY".equalsIgnoreCase(request.getOrderKind())) {
            return createBuyOrder(userNo, request);
        }

        if ("SELL".equalsIgnoreCase(request.getOrderKind())) {
            return createSellOrder(userNo, request);
        }

        throw new IllegalArgumentException("주문 구분이 올바르지 않습니다.");
    }

    private void validateOrderRequest(Long userNo, OrderRequest request) {
        if (userNo == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        if (request == null) {
            throw new IllegalArgumentException("주문 요청값이 없습니다.");
        }

        if (request.getStockCode() == null || request.getStockCode().isBlank()) {
            throw new IllegalArgumentException("종목코드는 필수입니다.");
        }

        if (request.getOrderKind() == null || request.getOrderKind().isBlank()) {
            throw new IllegalArgumentException("주문 구분은 필수입니다.");
        }

        if (request.getOrderType() == null || request.getOrderType().isBlank()) {
            throw new IllegalArgumentException("주문 유형은 필수입니다.");
        }

        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("주문 가격은 0보다 커야 합니다.");
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("주문 수량은 1주 이상이어야 합니다.");
        }
    }

	
	private OrderResponse createBuyOrder(Long userNo, OrderRequest request) {
	    Long accountNo = orderDao.selectActiveAccountNoByUserNo(userNo);

	    if (accountNo == null) {
	        throw new IllegalArgumentException("활성 계좌가 없습니다.");
	    }

	    BigDecimal orderAmount =
	            request.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

	    BigDecimal availableCash = orderDao.selectAvailableCashByAccountNo(accountNo);

	    if (availableCash == null || availableCash.compareTo(orderAmount) < 0) {
	        throw new IllegalArgumentException("주문 가능 금액이 부족합니다.");
	    }

	    
	    int orderResult = orderDao.insertOrder(userNo, accountNo, request, "FILLED");

	    if (orderResult == 0) {
	        throw new IllegalStateException("주문 등록에 실패했습니다.");
	    }

	    int balanceResult = orderDao.updateAccountBalanceForBuy(accountNo, orderAmount);

	    if (balanceResult == 0) {
	        throw new IllegalStateException("계좌 잔고 차감에 실패했습니다.");
	    }

	    int holdingResult = orderDao.updateHoldingForBuy(
	            accountNo,
	            request.getStockCode(),
	            request.getQuantity(),
	            request.getPrice()
	    );

	    if (holdingResult == 0) {
	        throw new IllegalStateException("보유 주식 반영에 실패했습니다.");
	    }

	    return OrderResponse.builder()
	            .stockCode(request.getStockCode())
	            .orderKind(request.getOrderKind())
	            .orderType(request.getOrderType())
	            .price(request.getPrice())
	            .quantity(request.getQuantity())
	            .status("FILLED")
	            .createdAt(new Date())
	            .build();
	}
	
	private OrderResponse createSellOrder(Long userNo, OrderRequest request) {
	    Long accountNo = orderDao.selectActiveAccountNoByUserNo(userNo);

	    if (accountNo == null) {
	        throw new IllegalArgumentException("활성 계좌가 없습니다.");
	    }

	    Long holdingQuantity =
	            orderDao.selectHoldingQuantityByAccountNoAndStockCode(
	                    accountNo,
	                    request.getStockCode()
	            );

	    if (holdingQuantity == null || holdingQuantity < request.getQuantity()) {
	        throw new IllegalArgumentException("보유 수량이 부족합니다.");
	    }

	    BigDecimal orderAmount =
	            request.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

	    
	    int orderResult = orderDao.insertOrder(userNo, accountNo, request, "FILLED");

	    if (orderResult == 0) {
	        throw new IllegalStateException("주문 등록에 실패했습니다.");
	    }

	    int holdingResult = orderDao.updateHoldingForSell(
	            accountNo,
	            request.getStockCode(),
	            request.getQuantity()
	    );

	    if (holdingResult == 0) {
	        throw new IllegalStateException("보유 주식 차감에 실패했습니다.");
	    }

	    int balanceResult = orderDao.updateAccountBalanceForSell(accountNo, orderAmount);

	    if (balanceResult == 0) {
	        throw new IllegalStateException("계좌 잔고 증가에 실패했습니다.");
	    }

	    return OrderResponse.builder()
	            .stockCode(request.getStockCode())
	            .orderKind(request.getOrderKind())
	            .orderType(request.getOrderType())
	            .price(request.getPrice())
	            .quantity(request.getQuantity())
	            .status("FILLED")
	            .createdAt(new Date())
	            .build();
	}
}
