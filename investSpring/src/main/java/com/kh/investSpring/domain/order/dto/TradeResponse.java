package com.kh.investSpring.domain.order.dto;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeResponse {
	
	private Long tradeId;        // 체결번호
    private String orderKind;    // BUY / SELL
    private String stockCode;    // 종목코드
    private String stockName;    // 종목명
    private BigDecimal price;    // 체결가격
    private Long quantity;       // 체결수량
    private Date executedAt;     // 체결시간
    
}
