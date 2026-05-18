package com.kh.investSpring.domain.search.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.investSpring.domain.search.dto.SearchIntegratedResponse;
import com.kh.investSpring.domain.search.dto.SearchSuggestResponse;
import com.kh.investSpring.domain.search.service.SearchService;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService service;

    // 자동완성
    @GetMapping("/suggest")
    public List<SearchSuggestResponse> suggest(@RequestParam String keyword) {
        return service.getSuggest(keyword);
    }

    // 통합 검색
    @GetMapping
    public SearchIntegratedResponse search(@RequestParam String keyword) {
        return service.search(keyword);
    }
}