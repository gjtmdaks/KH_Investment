package com.kh.investSpring.domain.order.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kh.investSpring.domain.order.dao.OrderDao;
import com.kh.investSpring.domain.order.dto.OrderHistoryResponse;
import com.kh.investSpring.domain.order.dto.TradeResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderQueryServiceImpl implements OrderQueryService {

	private final OrderDao orderDao;

    @Override
    public List<TradeResponse> getTradeHistory(Long userNo) {
        if (userNo == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        return orderDao.selectTradeHistoryByUserNo(userNo);
    }

    @Override
    public List<OrderHistoryResponse> getOrderHistory(Long userNo) {
        if (userNo == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        return orderDao.selectOrderHistoryByUserNo(userNo);
    }

}
