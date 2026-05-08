package com.kh.investSpring.domain.news.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 종목 사전 1행. STOCKS 테이블의 (STOCK_CODE, STOCK_NAME)을 메모리 사전으로 적재할 때 사용
 */
@Getter
@Setter
public class StockDictionaryEntry {

	private String stockCode;
	private String stockName;
}
