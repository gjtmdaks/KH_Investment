package com.kh.investSpring.domain.news.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public record NewsResponse(
		Long newsInfoId,
		String title,
		String description,
		String publisher,
		String primaryLabel,
		String keywordKind,
		String articleLink,
		@JsonFormat(shape = JsonFormat.Shape.STRING)
		Instant publishedAt,
		/**
		 * 본문에서 추출된 관련 종목 목록(최대 5개). 매칭 점수 순으로 정렬되어 있으며,
		 * 매칭 결과가 없거나 사전이 비어있으면 빈 리스트를 반환합니다.
		 */
		List<RelatedStock> relatedStocks) {
}
