package com.kh.investSpring.api.naver;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.investSpring.api.naver.dto.NaverNewsItemDto;
import com.kh.investSpring.api.naver.dto.NaverNewsSearchResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * 네이버 검색 API — 뉴스( https://openapi.naver.com/v1/search/news.json )
 */
@Component
@Slf4j
public class NaverNewsApiClient {

	private final RestClient restClient;
	private final ObjectMapper objectMapper;

	public NaverNewsApiClient(
			@Value("${naver.api.appkey:}") String clientId,
			@Value("${naver.api.appsecret:}") String clientSecret,
			ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.restClient = RestClient.builder()
				.baseUrl("https://openapi.naver.com")
				.defaultHeader("X-Naver-Client-Id", clientId == null ? "" : clientId)
				.defaultHeader("X-Naver-Client-Secret", clientSecret == null ? "" : clientSecret)
				.build();
	}

	public List<NaverNewsItemDto> searchNews(String query, int display) {
		if (query == null || query.isBlank()) {
			return List.of();
		}
		int d = Math.min(Math.max(display, 1), 100);
		try {
			String body = restClient.get()
					.uri(uriBuilder -> uriBuilder.path("/v1/search/news.json")
							.queryParam("query", query)
							.queryParam("display", d)
							.queryParam("sort", "date")
							.build())
					.retrieve()
					.body(String.class);
			if (body == null || body.isBlank()) {
				return List.of();
			}
			NaverNewsSearchResponse parsed = objectMapper.readValue(body, NaverNewsSearchResponse.class);
			if (parsed.items() == null) {
				return List.of();
			}
			return parsed.items();
		} catch (RestClientException e) {
			log.warn("네이버 뉴스 API HTTP 오류: {}", e.getMessage());
			return List.of();
		} catch (Exception e) {
			log.warn("네이버 뉴스 API 응답 파싱 실패: {}", e.getMessage());
			return List.of();
		}
	}
}
