package com.kh.investSpring.domain.search.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kh.investSpring.domain.search.dao.SearchDao;
import com.kh.investSpring.domain.search.dto.SearchIntegratedResponse;
import com.kh.investSpring.domain.search.dto.SearchNewsResponse;
import com.kh.investSpring.domain.search.dto.SearchStockResponse;
import com.kh.investSpring.domain.search.dto.SearchSuggestResponse;
import com.kh.investSpring.domain.stock.dto.StockKeywordSearchDto;
import com.kh.investSpring.domain.stock.service.StockService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SearchDao dao;
    private final StockService stockService;

    @Override
    public List<SearchSuggestResponse> getSuggest(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }

        return toSuggestResponses(
                stockService.searchByKeyword(keyword.trim(), 10)
        );
    }

    @Override
    public SearchIntegratedResponse search(String keyword) {
        String q = keyword.trim();
        List<SearchStockResponse> stocks = toStockResponses(
                stockService.searchByKeyword(q, 50)
        );
        List<SearchNewsResponse> news = dao.selectNews(q);

        return SearchIntegratedResponse.builder()
                .stocks(stocks)
                .news(news)
                .build();
    }

    private static List<SearchSuggestResponse> toSuggestResponses(
            List<StockKeywordSearchDto> hits
    ) {
        if (hits == null || hits.isEmpty()) {
            return List.of();
        }
        return hits.stream().map(SearchServiceImpl::toSuggest).toList();
    }

    private static List<SearchStockResponse> toStockResponses(
            List<StockKeywordSearchDto> hits
    ) {
        if (hits == null || hits.isEmpty()) {
            return List.of();
        }
        return hits.stream().map(SearchServiceImpl::toStock).toList();
    }

    private static SearchSuggestResponse toSuggest(StockKeywordSearchDto hit) {
        SearchSuggestResponse dto = new SearchSuggestResponse();
        dto.setStockCode(hit.getStockCode());
        dto.setStockName(hit.getStockName());
        dto.setMarketType(hit.getMarketType());
        return dto;
    }

    private static SearchStockResponse toStock(StockKeywordSearchDto hit) {
        SearchStockResponse dto = new SearchStockResponse();
        dto.setStockCode(hit.getStockCode());
        dto.setStockName(hit.getStockName());
        dto.setMarketType(hit.getMarketType());
        return dto;
    }
}