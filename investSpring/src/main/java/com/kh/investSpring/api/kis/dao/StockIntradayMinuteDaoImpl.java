package com.kh.investSpring.api.kis.dao;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.investSpring.api.kis.dto.StockIntradayMinuteCacheDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StockIntradayMinuteDaoImpl implements StockIntradayMinuteDao {

    private final SqlSessionTemplate session;

    @Override
    public List<StockIntradayMinuteCacheDto> selectByStockAndDate(
            String stockCode,
            LocalDate tradeDate
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("stockCode", stockCode);
        params.put("tradeDate", tradeDate);

        return session.selectList("api.selectIntradayMinutesByDate", params);
    }

    @Override
    public void mergeMinute(StockIntradayMinuteCacheDto dto) {
        session.insert("api.mergeIntradayMinute", dto);
    }
}
