package com.kh.investSpring.domain.news.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * NEWS_INFO_STOCK ⨝ STOCKS ⨝ STOCK_REALTIME_TICK 조회 결과 1행.
 *
 * {@link com.kh.investSpring.domain.news.dao.NewsDao#selectRelatedStocksByNewsIds(java.util.List)} 매퍼의 결과 매핑용
 */
@Getter
@Setter
public class NewsRelatedStockRow {

	private Long newsInfoId;
	private String stockCode;
	private String stockName;
	private Double changeRate;
}
