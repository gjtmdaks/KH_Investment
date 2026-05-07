package com.kh.investSpring.domain.news.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.investSpring.domain.news.dto.NewsResponse;
import com.kh.investSpring.domain.news.service.NewsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/public/news")
@RequiredArgsConstructor
public class NewsController {

	private final NewsService newsService;

	@GetMapping("/market")
	public List<NewsResponse> getMarketNews(@RequestParam(name = "size", defaultValue = "20") int size) {
		return newsService.getMarketNews(size);
	}

	@GetMapping("/stock/{stockCode}")
	public List<NewsResponse> getStockNews(
			@PathVariable String stockCode,
			@RequestParam(name = "size", defaultValue = "20") int size) {
		return newsService.getStockNews(stockCode, size);
	}
}
