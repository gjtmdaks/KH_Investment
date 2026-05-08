package com.kh.investSpring.domain.news.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.investSpring.domain.news.dto.NewsResponse;
import com.kh.investSpring.domain.news.service.NewsService;

import lombok.RequiredArgsConstructor;

/**
 * 단순 조회/필터링용 뉴스 API.
 * - 기존 /api/public/news/* 엔드포인트는 유지합니다.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NewsQueryController {

	private final NewsService newsService;

	/**
	 * GET /api/news?size=20&tag=삼성전자
	 * - tag(=primary_label)가 주어지면 해당 키워드로 필터링합니다.
	 */
	@GetMapping("/news")
	public List<NewsResponse> getNews(
			@RequestParam(name = "size", defaultValue = "20") int size,
			@RequestParam(name = "tag", required = false) String tag) {
		return newsService.getMarketNewsByTag(tag, size);
	}
}

