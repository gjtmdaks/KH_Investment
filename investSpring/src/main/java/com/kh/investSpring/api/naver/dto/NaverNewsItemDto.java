package com.kh.investSpring.api.naver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverNewsItemDto(
		String title,
		String link,
		String originallink,
		String description,
		String pubDate) {
}
