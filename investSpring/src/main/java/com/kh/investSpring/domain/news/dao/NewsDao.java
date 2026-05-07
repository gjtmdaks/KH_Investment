package com.kh.investSpring.domain.news.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kh.investSpring.domain.news.dto.NewsInfoEntity;

@Mapper
public interface NewsDao {

	void upsertNewsInfo(NewsInfoEntity row);

	Long selectNewsInfoIdByLink(@Param("articleLink") String articleLink);

	void mergeNewsInfoStock(@Param("newsInfoId") long newsInfoId, @Param("stockCode") String stockCode);

	List<NewsInfoEntity> selectRecentNewsInfo(@Param("limit") int limit);

	List<NewsInfoEntity> selectNewsInfoByStockCode(@Param("stockCode") String stockCode, @Param("limit") int limit);
}
