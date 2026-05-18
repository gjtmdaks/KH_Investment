package com.kh.investSpring.api.kis.chart;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kh.investSpring.api.kis.service.KisHistoryService;
import com.kh.investSpring.domain.stock.dao.StockDao;
import com.kh.investSpring.domain.stock.dto.StockScreenerDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChartHistoryPrewarmService {

    private static final String[] PERIODS = { "D", "W", "M" };
    private static final long PERIOD_GAP_MS = 3_000L;

    private final StockDao stockDao;
    private final KisHistoryService kisHistoryService;

    @Value("${chart.prewarm.max-stocks:30}")
    private int maxStocks;

    public void prewarmPopularStocks() {
        if (kisHistoryService.isBulkSyncInProgress()) {
            log.info("전체 history bulk sync 진행 중 — 차트 pre-warm 건너뜀");
            return;
        }

        List<String> targets = resolvePopularStockCodes();

        if (targets.isEmpty()) {
            log.warn("차트 pre-warm 대상 종목이 없습니다.");
            return;
        }

        log.info("차트 history pre-warm 시작 count={}", targets.size());

        for (String stockCode : targets) {
            if (kisHistoryService.isBulkSyncInProgress()) {
                log.info("bulk sync 시작 감지 — pre-warm 중단");
                break;
            }

            for (String period : PERIODS) {
                try {
                    kisHistoryService.syncHistory(stockCode, period);
                    Thread.sleep(PERIOD_GAP_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("차트 pre-warm 인터럽트 stockCode={}", stockCode);
                    return;
                } catch (Exception e) {
                    log.warn("차트 pre-warm 실패 stockCode={} period={}", stockCode, period, e);
                }
            }
        }

        log.info("차트 history pre-warm 완료");
    }

    private List<String> resolvePopularStockCodes() {
        Set<String> codes = new LinkedHashSet<>();

        try {
            String topVolume = stockDao.getTopVolumeStockCode();

            if (topVolume != null && !topVolume.isBlank()) {
                codes.add(topVolume.trim());
            }
        } catch (Exception e) {
            log.debug("거래대금 1위 종목 조회 실패: {}", e.getMessage());
        }

        appendCodes(codes, stockDao.getVolumeStocks());
        appendCodes(codes, stockDao.getPopularWatchlistStocks());

        List<String> result = new ArrayList<>();

        for (String code : codes) {
            if (result.size() >= maxStocks) {
                break;
            }

            result.add(code);
        }

        return result;
    }

    private static void appendCodes(Set<String> target, List<StockScreenerDto> rows) {
        if (rows == null) {
            return;
        }

        for (StockScreenerDto row : rows) {
            if (row.getStockCode() != null && !row.getStockCode().isBlank()) {
                target.add(row.getStockCode().trim());
            }
        }
    }
}
