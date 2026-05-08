package com.kh.investSpring.domain.news.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.kh.investSpring.domain.news.dto.NewsInfoEntity;

import lombok.extern.slf4j.Slf4j;

/**
 * DB 접근은 {@link NewsDao} MyBatis 매퍼를 사용합니다.
 */
@Slf4j
@Repository
public class NewsDaoImpl implements NewsDao {
	
	@Override
	public void upsertNewsInfo(NewsInfoEntity row) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Long selectNewsInfoIdByLink(String articleLink) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void mergeNewsInfoStock(long newsInfoId, String stockCode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<NewsInfoEntity> selectRecentNewsInfo(int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<NewsInfoEntity> selectNewsInfoByStockCode(String stockCode, int limit) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
