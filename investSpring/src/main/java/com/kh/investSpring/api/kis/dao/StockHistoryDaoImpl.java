package com.kh.investSpring.api.kis.dao;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public List<StockHistoryCacheDto> selectHistoryByRange(
            String stockCode,
            String periodType,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("stockCode", stockCode);
        params.put("periodType", periodType);
        params.put("fromDate", fromDate);
        params.put("toDate", toDate);

        return session.selectList("api.selectHistoryByRange", params);
    }
}