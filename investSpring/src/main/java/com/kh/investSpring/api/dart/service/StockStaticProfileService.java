package com.kh.investSpring.api.dart.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.kh.investSpring.api.dart.dao.corpInformationDao;
import com.kh.investSpring.api.dart.dto.StockStaticProfileResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockStaticProfileService {

    private final corpInformationDao corpInformationDao;

    public StockStaticProfileResponse getStaticProfile(String stockCode) {
        Map<String, Object> row = corpInformationDao.selectStaticProfileByStockCode(stockCode);

        if (row == null || row.isEmpty()) {
            return null;
        }

        return new StockStaticProfileResponse(
                value(row, "stockCode"),
                value(row, "stockName"),
                value(row, "marketType"),
                value(row, "sector"),
                value(row, "listedDate"),
                value(row, "status"),
                value(row, "corpCode"),
                value(row, "coName"),
                value(row, "issuedStock"),
                value(row, "declinedStock"),
                value(row, "treasuryStock"),
                value(row, "outstandingShares"),
                value(row, "shareholdingRatio"),
                value(row, "ownershipPercentage"));
    }

    private static String value(Map<String, Object> row, String key) {
        Object v = row.get(key);

        if (v == null) {
            v = row.get(key.toUpperCase());
        }

        return v == null ? null : String.valueOf(v);
    }
}
