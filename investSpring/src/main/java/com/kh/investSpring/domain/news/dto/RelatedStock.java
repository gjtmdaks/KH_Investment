package com.kh.investSpring.domain.news.dto;

/**
 * 뉴스 카드/모달에 노출되는 관련 종목 1건.
 *
 * 실시간 등락률(STOCK_REALTIME_TICK.CHANGE_RATE)이 없으면 {@code changeRate}는 null
 */
public record RelatedStock(
		String stockCode,
		String stockName,
		Double changeRate) {
}
