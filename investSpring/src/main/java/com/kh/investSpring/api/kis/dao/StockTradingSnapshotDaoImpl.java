package com.kh.investSpring.api.kis.dao;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StockTradingSnapshotDaoImpl implements StockTradingSnapshotDao {

    private final SqlSessionTemplate session;

    @Override
    public int insertSnapshotsFromCurrent() {
        return session.insert("api.insertTradingSnapshotsFromCurrent");
    }

    @Override
    public int deleteSnapshotsOlderThanDays(int retentionDays) {
        return session.delete("api.deleteOldTradingSnapshots", retentionDays);
    }
}
