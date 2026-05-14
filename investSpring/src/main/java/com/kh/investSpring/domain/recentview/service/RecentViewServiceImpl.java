package com.kh.investSpring.domain.recentview.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.investSpring.domain.recentview.dao.RecentViewDao;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RecentViewServiceImpl implements RecentViewService {

    private final RecentViewDao dao;

    @Override
    public void saveRecentView(Long userNo, String stockCode) {
        if (userNo == null) {
            return;
        }

        dao.upsertRecentView(userNo, stockCode);
        dao.deleteOverflowRecentViews(userNo);
    }
}