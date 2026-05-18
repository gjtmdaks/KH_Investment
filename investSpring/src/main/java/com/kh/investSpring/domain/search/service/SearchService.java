package com.kh.investSpring.domain.search.service;

import java.util.List;

import com.kh.investSpring.domain.search.dto.SearchIntegratedResponse;
import com.kh.investSpring.domain.search.dto.SearchSuggestResponse;

public interface SearchService {

    List<SearchSuggestResponse> getSuggest(String keyword);

    SearchIntegratedResponse search(String keyword);
}