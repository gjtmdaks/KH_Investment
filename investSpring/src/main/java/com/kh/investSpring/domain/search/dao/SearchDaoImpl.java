package com.kh.investSpring.domain.search.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.investSpring.domain.search.dto.SearchNewsResponse;
import com.kh.investSpring.domain.search.dto.SearchStockResponse;
import com.kh.investSpring.domain.search.dto.SearchSuggestResponse;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SearchDaoImpl implements SearchDao {

    private final SqlSessionTemplate session;

    @Override
    public List<SearchSuggestResponse> selectSuggest(String keyword) {
        return session.selectList("search.selectSuggest", keyword);
    }

    @Override
    public List<SearchStockResponse> selectStocks(String keyword) {
        return session.selectList("search.selectStocks", keyword);
    }

    @Override
    public List<SearchNewsResponse> selectNews(String keyword) {
        return session.selectList("search.selectNews", keyword);
    }
}