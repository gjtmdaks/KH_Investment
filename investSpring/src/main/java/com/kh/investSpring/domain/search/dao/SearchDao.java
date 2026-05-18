package com.kh.investSpring.domain.search.dao;

import java.util.List;

import com.kh.investSpring.domain.search.dto.SearchNewsResponse;
import com.kh.investSpring.domain.search.dto.SearchStockResponse;
import com.kh.investSpring.domain.search.dto.SearchSuggestResponse;

public interface SearchDao {

    List<SearchSuggestResponse> selectSuggest(String keyword);

    List<SearchStockResponse> selectStocks(String keyword);

    List<SearchNewsResponse> selectNews(String keyword);
}