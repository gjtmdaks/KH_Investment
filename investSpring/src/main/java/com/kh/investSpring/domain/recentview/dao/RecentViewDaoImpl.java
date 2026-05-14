package com.kh.investSpring.domain.recentview.dao;

import java.util.HashMap;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RecentViewDaoImpl implements RecentViewDao {

    private final SqlSessionTemplate session;

    @Override
    public void upsertRecentView(Long userNo, String stockCode) {
        Map<String, Object> param = new HashMap<>();
        param.put("userNo", userNo);
        param.put("stockCode", stockCode);

        session.insert("recentView.upsertRecentView", param);
    }

    @Override
    public void deleteOverflowRecentViews(Long userNo) {
        session.delete("recentView.deleteOverflowRecentViews", userNo);
    }
}