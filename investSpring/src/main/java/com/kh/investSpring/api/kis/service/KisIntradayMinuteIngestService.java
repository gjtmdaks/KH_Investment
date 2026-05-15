package com.kh.investSpring.api.kis.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kh.investSpring.api.kis.config.KisProperties;
import com.kh.investSpring.api.kis.dao.StockIntradayMinuteDao;
import com.kh.investSpring.api.kis.dto.StockIntradayMinuteCacheDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KisIntradayMinuteIngestService {

    private static final String TR_ID = "FHKST03010200";
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final LocalTime SESSION_START = LocalTime.of(9, 0);
    private static final LocalTime SESSION_END = LocalTime.of(15, 30);

    private final KisProperties properties;
    private final KisMinuteChartApiClient kisMinuteChartApiClient;
    private final StockIntradayMinuteDao stockIntradayMinuteDao;

    public void ingestTradingDay(String stockCode, LocalDate tradeDate) throws IOException, InterruptedException {
        LocalDate today = LocalDate.now(SEOUL);
        if (!tradeDate.equals(today)) {
            log.debug("분봉 KIS 적재는 당일만 stockCode={} tradeDate={}", stockCode, tradeDate);

            return;
        }

        ZonedDateTime nowSeoul = ZonedDateTime.now(SEOUL);
        if (nowSeoul.toLocalDate().equals(tradeDate)
                && nowSeoul.toLocalTime().isBefore(SESSION_START)) {
            log.debug("장 시작 전 분봉 적재 생략 stockCode={}", stockCode);

            return;
        }

        String initialCursor = initialCursorHHMMSS(tradeDate);

        List<StockIntradayMinuteCacheDto> ordered =
                KisMinuteIngestLoop.collectTradingDay(
                        stockCode,
                        tradeDate,
                        initialCursor,
                        cursor -> requestBatch(stockCode, cursor)
                );

        for (StockIntradayMinuteCacheDto dto : ordered) {
            stockIntradayMinuteDao.mergeMinute(dto);
        }

        log.info(
                "분봉 캐시 저장 완료 stockCode={} tradeDate={} bars={}",
                stockCode,
                tradeDate,
                ordered.size()
        );
    }

    private String initialCursorHHMMSS(LocalDate tradeDate) {
        ZonedDateTime now = ZonedDateTime.now(SEOUL);

        if (!now.toLocalDate().equals(tradeDate)) {
            return "153000";
        }

        LocalTime t = now.toLocalTime();

        if (t.isBefore(SESSION_START)) {
            return "090000";
        }

        if (t.isAfter(SESSION_END)) {
            return "153000";
        }

        return String.format("%02d%02d%02d", t.getHour(), t.getMinute(), 0);
    }

    private List<java.util.Map<String, Object>> requestBatch(String stockCode, String fidInputHour1)
            throws IOException, InterruptedException {
        String url =
                properties.getBaseUrl()
                + "/uapi/domestic-stock/v1/quotations/inquire-time-itemchartprice"
                + "?FID_COND_MRKT_DIV_CODE=J"
                + "&FID_INPUT_ISCD=" + stockCode
                + "&FID_INPUT_HOUR_1=" + fidInputHour1
                + "&FID_ETC_CLS_CODE="
                + "&FID_PW_DATA_INCU_YN=Y";

        return kisMinuteChartApiClient.fetch(url, TR_ID, stockCode, "당일분봉");
    }
}
