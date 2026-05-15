package com.kh.investSpring.domain.stock.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RealtimeSectionResponseDto {

    private List<StockScreenerDto> surging;
    private List<StockScreenerDto> falling;
    private List<StockScreenerDto> active;
}