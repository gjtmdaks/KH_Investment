package com.kh.investSpring.api.kis.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.investSpring.api.kis.dto.StockHistoryCacheDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StockHistoryDaoImpl implements StockHistoryDao {

    private final SqlSessionTemplate session;

    @Override
    public List<String> selectAllStockCodes() {
        return session.selectList("api.selectAllStockCodes");
    }

    @Override
    public void mergeHistory(StockHistoryCacheDto dto) {
        session.insert("api.mergeHistory", dto);
    }
}