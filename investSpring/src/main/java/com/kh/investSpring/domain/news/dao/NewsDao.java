package com.kh.investSpring.domain.news.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.kh.investSpring.domain.news.dto.NewsInfoEntity;
import com.kh.investSpring.domain.news.dto.NewsRelatedStockRow;
import com.kh.investSpring.domain.news.dto.StockDictionaryEntry;

public interface NewsDao {

	void upsertNewsInfo(NewsInfoEntity row);

	Long selectNewsInfoIdByLink(@Param("articleLink") String articleLink);

	void mergeNewsInfoStock(@Param("newsInfoId") long newsInfoId, @Param("stockCode") String stockCode);

	void updateNewsTitleDescription(
			@Param("newsInfoId") long newsInfoId,
			@Param("newsTitle") String newsTitle,
			@Param("newsDescription") String newsDescription);

	List<NewsInfoEntity> selectRecentNewsInfo(@Param("limit") int limit);

	List<NewsInfoEntity> selectNewsInfoByStockCode(@Param("stockCode") String stockCode, @Param("limit") int limit);

	/**
	 * 종목명 매칭용 사전 로드. ACTIVE 상태의 KOSPI/KOSDAQ/KONEX 종목 전체를 반환.
	 */
	List<StockDictionaryEntry> selectAllActiveStockDictionary();

	/**
	 * 뉴스 ID 목록에 매핑된 관련 종목들을 종목명·최신 등락률과 함께 일괄 조회.
	 */
	List<NewsRelatedStockRow> selectRelatedStocksByNewsIds(@Param("ids") List<Long> ids);
}
