package com.kh.investSpring.domain.recentview.dao;

public interface RecentViewDao {
	
	void upsertRecentView(Long userNo, String stockCode);

	void deleteOverflowRecentViews(Long userNo);

}
