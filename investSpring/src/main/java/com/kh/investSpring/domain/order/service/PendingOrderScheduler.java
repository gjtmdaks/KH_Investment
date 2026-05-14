package com.kh.investSpring.domain.order.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.investSpring.domain.order.dao.OrderDao;
import com.kh.investSpring.domain.order.dto.PendingOrderDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PendingOrderScheduler {

    private final OrderDao orderDao;

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void executePendingOrders() {
        List<PendingOrderDto> pendingOrders = orderDao.selectExecutablePendingOrders();

        if (pendingOrders == null || pendingOrders.isEmpty()) {
            return;
        }
        
        for (PendingOrderDto order : pendingOrders) {
            executeOne(order);
        }
    }
    
    private void executeOne(PendingOrderDto order) {
        BigDecimal executionPrice = order.getCurrentPrice();

        if (executionPrice == null || executionPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        int statusResult = orderDao.updateOrderStatusFilledByOrderId(order.getOrderId());

        if (statusResult == 0) {
            return;
        }

        int tradeResult = orderDao.insertTrade(
                order.getOrderId(),
                executionPrice,
                order.getQuantity()
        );

        if (tradeResult == 0) {
            throw new IllegalStateException("체결 내역 등록에 실패했습니다.");
        }

        if ("BUY".equalsIgnoreCase(order.getOrderKind())) {
            executePendingBuy(order, executionPrice);
            return;
        }

        if ("SELL".equalsIgnoreCase(order.getOrderKind())) {
            executePendingSell(order, executionPrice);
            return;
        }

        throw new IllegalArgumentException("주문 구분이 올바르지 않습니다.");
    }

    private void executePendingBuy(
            PendingOrderDto order,
            BigDecimal executionPrice
    ) {
        BigDecimal lockedAmount =
                order.getOrderPrice()
                        .multiply(BigDecimal.valueOf(order.getQuantity()));

        BigDecimal executedAmount =
                executionPrice
                        .multiply(BigDecimal.valueOf(order.getQuantity()));

        BigDecimal refundAmount = lockedAmount.subtract(executedAmount);

        if (refundAmount.compareTo(BigDecimal.ZERO) < 0) {
            refundAmount = BigDecimal.ZERO;
        }

        int balanceResult = orderDao.updateAccountBalanceForPendingBuyFilled(
                order.getAccountNo(),
                lockedAmount,
                refundAmount
        );

        if (balanceResult == 0) {
            throw new IllegalStateException("예약 매수금 처리에 실패했습니다.");
        }

        int holdingResult = orderDao.updateHoldingForBuy(
                order.getAccountNo(),
                order.getStockCode(),
                order.getQuantity(),
                executionPrice
        );

        if (holdingResult == 0) {
            throw new IllegalStateException("예약 매수 보유 주식 반영에 실패했습니다.");
        }
    }

    private void executePendingSell(
            PendingOrderDto order,
            BigDecimal executionPrice
    ) {
        BigDecimal executedAmount =
                executionPrice
                        .multiply(BigDecimal.valueOf(order.getQuantity()));

        int holdingResult = orderDao.updateHoldingForSell(
                order.getAccountNo(),
                order.getStockCode(),
                order.getQuantity()
        );

        if (holdingResult == 0) {
            throw new IllegalStateException("예약 매도 보유 주식 차감에 실패했습니다.");
        }

        int balanceResult = orderDao.updateAccountBalanceForSell(
                order.getAccountNo(),
                executedAmount
        );

        if (balanceResult == 0) {
            throw new IllegalStateException("예약 매도 현금 증가에 실패했습니다.");
        }
    }
}