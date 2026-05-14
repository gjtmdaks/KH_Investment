package com.kh.investSpring.domain.order.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.investSpring.domain.order.dto.OrderHistoryResponse;
import com.kh.investSpring.domain.order.dto.OrderPriceUpdateRequest;
import com.kh.investSpring.domain.order.dto.OrderRequest;
import com.kh.investSpring.domain.order.dto.OrderResponse;
import com.kh.investSpring.domain.order.dto.TradeResponse;
import com.kh.investSpring.domain.order.service.OrderCommandService;
import com.kh.investSpring.domain.order.service.OrderQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
	
	private final OrderCommandService orderCommandService;
	private final OrderQueryService orderQueryService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            Authentication authentication,
            @RequestBody OrderRequest request
    ) {
        Long userNo = Long.valueOf(authentication.getName());

        OrderResponse response = orderCommandService.createOrder(userNo, request);

        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/trades")
    public ResponseEntity<List<TradeResponse>> getTradeHistory(
            Authentication authentication
    ) {
        Long userNo = Long.valueOf(authentication.getName());

        List<TradeResponse> trades = orderQueryService.getTradeHistory(userNo);

        return ResponseEntity.ok(trades);
    }
    
    @GetMapping("/history")
    public ResponseEntity<List<OrderHistoryResponse>> getOrderHistory(
            Authentication authentication
    ) {
        Long userNo = Long.valueOf(authentication.getName());

        List<OrderHistoryResponse> orders = orderQueryService.getOrderHistory(userNo);

        return ResponseEntity.ok(orders);
    }
    
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(
            Authentication authentication,
            @PathVariable Long orderId
    ) {
        Long userNo = Long.valueOf(authentication.getName());

        orderCommandService.cancelOrder(userNo, orderId);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{orderId}/price")
    public ResponseEntity<Void> updateOrderPrice(
            Authentication authentication,
            @PathVariable Long orderId,
            @RequestBody OrderPriceUpdateRequest request
    ) {
        Long userNo = Long.valueOf(authentication.getName());

        orderCommandService.updateOrderPrice(userNo, orderId, request);

        return ResponseEntity.ok().build();
    }
}
