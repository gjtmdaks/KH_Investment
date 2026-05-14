package com.kh.investSpring.domain.order.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderPriceUpdateRequest {

    private BigDecimal price;
}