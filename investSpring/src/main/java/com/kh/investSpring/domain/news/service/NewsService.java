package com.kh.investSpring.domain.news.service;

import java.util.List;

import com.kh.investSpring.domain.news.dto.NewsResponse;

public interface NewsService {

	List<NewsResponse> getMarketNews(int size);

	List<NewsResponse> getStockNews(String stockCode, int size);
}
