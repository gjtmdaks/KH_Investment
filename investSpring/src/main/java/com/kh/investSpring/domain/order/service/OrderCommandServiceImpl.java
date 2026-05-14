package com.kh.investSpring.domain.order.service;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.investSpring.domain.order.dao.OrderDao;
import com.kh.investSpring.domain.order.dto.OrderPriceUpdateRequest;
import com.kh.investSpring.domain.order.dto.OrderRequest;
import com.kh.investSpring.domain.order.dto.OrderResponse;
import com.kh.investSpring.domain.order.dto.PendingOrderManageDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderCommandServiceImpl implements OrderCommandService {

    private final OrderDao orderDao;

    @Override
    @Transactional
    public OrderResponse createOrder(Long userNo, OrderRequest request) { // 매수인지 매도인지 구분
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

	// 매수 영역
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

        if ("MARKET".equalsIgnoreCase(request.getOrderType())) {
            return createFilledBuyOrder(userNo, accountNo, request, orderAmount);
        }

        if ("LIMIT".equalsIgnoreCase(request.getOrderType())) {
            return createPendingBuyOrder(userNo, accountNo, request, orderAmount);
        }

        throw new IllegalArgumentException("주문 유형이 올바르지 않습니다.");
    }
    // 즉시 매수
    private OrderResponse createFilledBuyOrder(
            Long userNo,
            Long accountNo,
            OrderRequest request,
            BigDecimal orderAmount
    ) {
        Long orderId = orderDao.selectNextOrderId();

        int orderResult = orderDao.insertOrder(
                orderId,
                userNo,
                accountNo,
                request,
                "FILLED"
        );

        if (orderResult == 0) {
            throw new IllegalStateException("주문 등록에 실패했습니다.");
        }

        int tradeResult = orderDao.insertTrade(
                orderId,
                request.getPrice(),
                request.getQuantity()
        );

        if (tradeResult == 0) {
            throw new IllegalStateException("체결 내역 등록에 실패했습니다.");
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
    // 지정가 매수
    private OrderResponse createPendingBuyOrder(
            Long userNo,
            Long accountNo,
            OrderRequest request,
            BigDecimal orderAmount
    ) {
        Long orderId = orderDao.selectNextOrderId();

        int orderResult = orderDao.insertOrder(
                orderId,
                userNo,
                accountNo,
                request,
                "PENDING"
        );

        if (orderResult == 0) {
            throw new IllegalStateException("주문 예약 등록에 실패했습니다.");
        }

        int balanceResult = orderDao.updateAccountBalanceForPendingBuy(
                accountNo,
                orderAmount
        );

        if (balanceResult == 0) {
            throw new IllegalStateException("주문 예약금 처리에 실패했습니다.");
        }

        return OrderResponse.builder()
                .stockCode(request.getStockCode())
                .orderKind(request.getOrderKind())
                .orderType(request.getOrderType())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .status("PENDING")
                .createdAt(new Date())
                .build();
    }
    
    
	//매도 영역
    private OrderResponse createSellOrder(Long userNo, OrderRequest request) {
        Long accountNo = orderDao.selectActiveAccountNoByUserNo(userNo);

        if (accountNo == null) {
            throw new IllegalArgumentException("활성 계좌가 없습니다.");
        }

        Long sellableQuantity =
                orderDao.selectSellableQuantityByAccountNoAndStockCode(
                        accountNo,
                        request.getStockCode()
                );

        if (sellableQuantity == null || sellableQuantity < request.getQuantity()) {
            throw new IllegalArgumentException("매도 가능 수량이 부족합니다.");
        }

        BigDecimal orderAmount =
                request.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        if ("MARKET".equalsIgnoreCase(request.getOrderType())) {
            return createFilledSellOrder(userNo, accountNo, request, orderAmount);
        }

        if ("LIMIT".equalsIgnoreCase(request.getOrderType())) {
            return createPendingSellOrder(userNo, accountNo, request);
        }

        throw new IllegalArgumentException("주문 유형이 올바르지 않습니다.");
    }
    // 즉시 매도
    private OrderResponse createFilledSellOrder(
            Long userNo,
            Long accountNo,
            OrderRequest request,
            BigDecimal orderAmount
    ) {
        Long orderId = orderDao.selectNextOrderId();

        int orderResult = orderDao.insertOrder(
                orderId,
                userNo,
                accountNo,
                request,
                "FILLED"
        );

        if (orderResult == 0) {
            throw new IllegalStateException("주문 등록에 실패했습니다.");
        }

        int tradeResult = orderDao.insertTrade(
                orderId,
                request.getPrice(),
                request.getQuantity()
        );

        if (tradeResult == 0) {
            throw new IllegalStateException("체결 내역 등록에 실패했습니다.");
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
    
    // 지정가 매도 예약
    private OrderResponse createPendingSellOrder(
            Long userNo,
            Long accountNo,
            OrderRequest request
    ) {
        Long orderId = orderDao.selectNextOrderId();

        int orderResult = orderDao.insertOrder(
                orderId,
                userNo,
                accountNo,
                request,
                "PENDING"
        );

        if (orderResult == 0) {
            throw new IllegalStateException("매도 예약 등록에 실패했습니다.");
        }

        return OrderResponse.builder()
                .stockCode(request.getStockCode())
                .orderKind(request.getOrderKind())
                .orderType(request.getOrderType())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .status("PENDING")
                .createdAt(new Date())
                .build();
    }

    @Override
    @Transactional
    public void cancelOrder(Long userNo, Long orderId) {
        if (userNo == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        if (orderId == null) {
            throw new IllegalArgumentException("주문번호가 필요합니다.");
        }

        PendingOrderManageDto order =
                orderDao.selectPendingOrderForManage(userNo, orderId);

        if (order == null) {
            throw new IllegalArgumentException("취소 가능한 주문을 찾을 수 없습니다.");
        }

        BigDecimal orderAmount =
                order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));

        if ("BUY".equalsIgnoreCase(order.getOrderKind())) {
            int balanceResult = orderDao.updateAccountBalanceForCancelPendingBuy(
                    order.getAccountNo(),
                    orderAmount
            );

            if (balanceResult == 0) {
                throw new IllegalStateException("예약 매수금 반환에 실패했습니다.");
            }
        }

        int cancelResult = orderDao.updateOrderStatusCanceledByOrderId(orderId);

        if (cancelResult == 0) {
            throw new IllegalStateException("주문 취소에 실패했습니다.");
        }
    }

    @Override
    @Transactional
    public void updateOrderPrice(
            Long userNo,
            Long orderId,
            OrderPriceUpdateRequest request
    ) {
        if (userNo == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        if (orderId == null) {
            throw new IllegalArgumentException("주문번호가 필요합니다.");
        }

        if (request == null || request.getPrice() == null
                || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("변경할 주문 가격은 0보다 커야 합니다.");
        }

        PendingOrderManageDto order =
                orderDao.selectPendingOrderForManage(userNo, orderId);

        if (order == null) {
            throw new IllegalArgumentException("가격 변경 가능한 주문을 찾을 수 없습니다.");
        }

        BigDecimal oldOrderAmount =
                order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));

        BigDecimal newOrderAmount =
                request.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));

        if ("BUY".equalsIgnoreCase(order.getOrderKind())) {
            int balanceResult =
                    orderDao.updateAccountBalanceForUpdatePendingBuyPrice(
                            order.getAccountNo(),
                            oldOrderAmount,
                            newOrderAmount
                    );

            if (balanceResult == 0) {
                throw new IllegalStateException("주문 가격 변경에 필요한 주문 가능 금액이 부족합니다.");
            }
        }

        int updateResult = orderDao.updateOrderPriceByOrderId(
                orderId,
                request.getPrice()
        );

        if (updateResult == 0) {
            throw new IllegalStateException("주문 가격 변경에 실패했습니다.");
        }
    }
    
}
