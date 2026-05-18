package com.kh.investSpring.domain.search.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kh.investSpring.domain.search.dao.SearchDao;
import com.kh.investSpring.domain.search.dto.SearchIntegratedResponse;
import com.kh.investSpring.domain.search.dto.SearchNewsResponse;
import com.kh.investSpring.domain.search.dto.SearchStockResponse;
import com.kh.investSpring.domain.search.dto.SearchSuggestResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SearchDao dao;

    @Override
    public List<SearchSuggestResponse> getSuggest(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }

        return dao.selectSuggest(keyword.trim());
    }

    @Override
    public SearchIntegratedResponse search(String keyword) {
        String q = keyword.trim();
        List<SearchStockResponse> stocks = dao.selectStocks(q);
        List<SearchNewsResponse> news = dao.selectNews(q);

        return SearchIntegratedResponse.builder()
                .stocks(stocks)
                .news(news)
                .build();
    }
}