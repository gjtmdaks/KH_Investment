package com.kh.investSpring.api.kis.dao;

public interface StockTradingSnapshotDao {

    int insertSnapshotsFromCurrent();

    int deleteSnapshotsOlderThanDays(int retentionDays);
}
