package com.kh.investSpring.domain.news.dto;

/**
 * 뉴스 카드/모달에 노출되는 관련 종목 1건.
 *
 * <p>등락률은 1차로 STOCK_REALTIME_TICK 조인·배치 ingest 후
 * {@code NewsServiceImpl}에서 KIS 전일대비율로 보강될 수 있습니다.</p>
 */
public record RelatedStock(
		String stockCode,
		String stockName,
		Double changeRate) {
}
