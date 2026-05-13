package com.kh.investSpring.domain.order.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderRequest {
	
    private String stockCode;      // 종목코드
    private String orderKind; // BUY / SELL
    private String orderType; // MARKET / LIMIT
    private BigDecimal price;
    private Long quantity;         
}
