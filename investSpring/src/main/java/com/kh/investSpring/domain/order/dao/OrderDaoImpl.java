package com.kh.investSpring.domain.order.dao;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.investSpring.domain.order.dto.OrderRequest;
import com.kh.investSpring.domain.order.dto.TradeResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
@RequiredArgsConstructor
public class OrderDaoImpl implements OrderDao{
	
	private final SqlSessionTemplate session;
	
	@Override
    public Long selectActiveAccountNoByUserNo(Long userNo) {
        return session.selectOne("order.selectActiveAccountNoByUserNo", userNo);
    }

    @Override
    public BigDecimal selectAvailableCashByAccountNo(Long accountNo) {
        return session.selectOne("order.selectAvailableCashByAccountNo", accountNo);
    }

    @Override
    public Long selectHoldingQuantityByAccountNoAndStockCode(
            Long accountNo,
            String stockCode
    ) {
        Map<String, Object> param = new HashMap<>();
        param.put("accountNo", accountNo);
        param.put("stockCode", stockCode);

        return session.selectOne(
                "order.selectHoldingQuantityByAccountNoAndStockCode",
                param
        );
    }

    @Override
    public int insertOrder(
            Long userNo,
            Long accountNo,
            OrderRequest request,
            String status
    ) {
        Map<String, Object> param = new HashMap<>();
        param.put("userNo", userNo);
        param.put("accountNo", accountNo);
        param.put("stockCode", request.getStockCode());
        param.put("orderKind", request.getOrderKind());
        param.put("orderType", request.getOrderType());
        param.put("price", request.getPrice());
        param.put("quantity", request.getQuantity());
        param.put("status", status);

        return session.insert("order.insertOrder", param);
    }

    @Override
    public int updateAccountBalanceForBuy(
            Long accountNo,
            BigDecimal orderAmount
    ) {
        Map<String, Object> param = new HashMap<>();
        param.put("accountNo", accountNo);
        param.put("orderAmount", orderAmount);

        return session.update("order.updateAccountBalanceForBuy", param);
    }

    @Override
    public int updateAccountBalanceForSell(
            Long accountNo,
            BigDecimal orderAmount
    ) {
        Map<String, Object> param = new HashMap<>();
        param.put("accountNo", accountNo);
        param.put("orderAmount", orderAmount);

        return session.update("order.updateAccountBalanceForSell", param);
    }

    @Override
    public int updateHoldingForBuy(
            Long accountNo,
            String stockCode,
            Long quantity,
            BigDecimal price
    ) {
        Map<String, Object> param = new HashMap<>();
        param.put("accountNo", accountNo);
        param.put("stockCode", stockCode);
        param.put("quantity", quantity);
        param.put("price", price);

        return session.update("order.updateHoldingForBuy", param);
    }

    @Override
    public int updateHoldingForSell(
            Long accountNo,
            String stockCode,
            Long quantity
    ) {
        Map<String, Object> param = new HashMap<>();
        param.put("accountNo", accountNo);
        param.put("stockCode", stockCode);
        param.put("quantity", quantity);

        return session.update("order.updateHoldingForSell", param);
    }
    
    // 오더
    @Override
    public Long selectNextOrderId() {
        return session.selectOne("order.selectNextOrderId");
    }
    @Override
    public int insertTrade(Long orderId, BigDecimal price, Long quantity) {
        Map<String, Object> param = new HashMap<>();
        param.put("orderId", orderId);
        param.put("price", price);
        param.put("quantity", quantity);

        return session.insert("order.insertTrade", param);
    }
    @Override
    public int insertOrder(
            Long orderId,
            Long userNo,
            Long accountNo,
            OrderRequest request,
            String status
    ) {
        Map<String, Object> param = new HashMap<>();
        param.put("orderId", orderId);
        param.put("userNo", userNo);
        param.put("accountNo", accountNo);
        param.put("stockCode", request.getStockCode());
        param.put("orderKind", request.getOrderKind());
        param.put("orderType", request.getOrderType());
        param.put("price", request.getPrice());
        param.put("quantity", request.getQuantity());
        param.put("status", status);

        return session.insert("order.insertOrder", param);
    }

    
	@Override
	public List<TradeResponse> selectTradeHistoryByUserNo(Long userNo) {
		// TODO Auto-generated method stub
		return session.selectList("order.selectTradeHistoryByUserNo", userNo);
	}
}
