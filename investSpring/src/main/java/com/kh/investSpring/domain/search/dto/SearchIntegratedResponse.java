package com.kh.investSpring.domain.search.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchIntegratedResponse {

    private List<SearchStockResponse> stocks;
    private List<SearchNewsResponse> news;
}