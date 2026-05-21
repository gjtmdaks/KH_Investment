package com.kh.investSpring.api.kis.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.kh.investSpring.api.kis.config.KisProperties;
import com.kh.investSpring.domain.stock.dao.StockDao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisSubscriptionTargetSelector {

    private final KisProperties kisProperties;
    private final StockDao stockDao;

    public LinkedHashSet<String> selectTargetStockCodes() {
        int max = Math.max(1, kisProperties.getWebsocketMaxSubscriptions());
        int demandSlots = Math.min(
                Math.max(0, kisProperties.getWebsocketDemandSlots()),
                max - 1);
        int baseSlots = max - demandSlots;

        LinkedHashSet<String> target = new LinkedHashSet<>();

        addCodes(target, stockDao.selectTopTradingValueStockCodes(baseSlots), max);

        if (demandSlots > 0 && target.size() < max) {
            int candidateLimit = demandSlots * 2;
            List<String> demandCandidates = new ArrayList<>();
            demandCandidates.addAll(
                    stockDao.selectRecentViewDemandStockCodes(
                            candidateLimit,
                            kisProperties.getWebsocketDemandRecentMinutes()));
            demandCandidates.addAll(
                    stockDao.selectWatchlistDemandStockCodes(candidateLimit));
            addCodes(target, demandCandidates, max);
        }

        if (target.size() < max) {
            addCodes(target, stockDao.selectTopTradingValueStockCodes(max), max);
        }

        log.info(
                "KIS WS 구독 대상 산출 완료 size={}, max={}, demandSlots={}",
                target.size(),
                max,
                demandSlots);

        return target;
    }

    private static void addCodes(Set<String> target, List<String> codes, int max) {
        if (codes == null) {
            return;
        }
        for (String code : codes) {
            if (target.size() >= max) {
                return;
            }
            if (code == null) {
                continue;
            }
            String trimmed = code.trim();
            if (!trimmed.isEmpty()) {
                target.add(trimmed);
            }
        }
    }
}
