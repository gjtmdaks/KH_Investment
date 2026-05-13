package com.kh.investSpring.domain.order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.investSpring.domain.order.dto.OrderRequest;
import com.kh.investSpring.domain.order.dto.OrderResponse;
import com.kh.investSpring.domain.order.service.OrderCommandService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
	
	private final OrderCommandService orderCommandService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            Authentication authentication,
            @RequestBody OrderRequest request
    ) {
        Long userNo = Long.valueOf(authentication.getName());

        OrderResponse response = orderCommandService.createOrder(userNo, request);

        return ResponseEntity.ok(response);
    }
}
