package com.kh.investSpring.domain.news.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;

public record NewsResponse(
		Long newsInfoId,
		String title,
		String description,
		String publisher,
		String articleLink,
		@JsonFormat(shape = JsonFormat.Shape.STRING)
		Instant publishedAt) {
}
