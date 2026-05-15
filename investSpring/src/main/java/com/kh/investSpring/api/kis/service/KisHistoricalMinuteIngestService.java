package com.kh.investSpring.api.kis.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
public class KisHistoricalMinuteIngestService {

    private static final String TR_ID = "FHKST03010230";
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter BASIC_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    private final KisProperties properties;
    private final KisMinuteChartApiClient kisMinuteChartApiClient;
    private final StockIntradayMinuteDao stockIntradayMinuteDao;

    public void ingestTradingDay(String stockCode, LocalDate tradeDate)
            throws IOException, InterruptedException {
        LocalDate today = LocalDate.now(SEOUL);

        if (tradeDate.equals(today)) {
            log.debug("과거 분봉 TR은 당일 제외 stockCode={} tradeDate={}", stockCode, tradeDate);

            return;
        }

        if (tradeDate.isAfter(today)) {
            log.debug("미래 거래일 분봉 적재 생략 stockCode={} tradeDate={}", stockCode, tradeDate);

            return;
        }

        LocalDate retentionFloor = today.minusYears(1);

        if (tradeDate.isBefore(retentionFloor)) {
            log.debug(
                    "KIS 분봉 보관 한도 초과 stockCode={} tradeDate={} floor={}",
                    stockCode,
                    tradeDate,
                    retentionFloor
            );

            return;
        }

        String dateParam = tradeDate.format(BASIC_DATE);
        String initialCursor = "153000";

        List<StockIntradayMinuteCacheDto> ordered =
                KisMinuteIngestLoop.collectTradingDay(
                        stockCode,
                        tradeDate,
                        initialCursor,
                        cursor -> requestBatch(stockCode, dateParam, cursor)
                );

        for (StockIntradayMinuteCacheDto dto : ordered) {
            stockIntradayMinuteDao.mergeMinute(dto);
        }

        log.info(
                "과거 분봉 캐시 저장 완료 stockCode={} tradeDate={} bars={}",
                stockCode,
                tradeDate,
                ordered.size()
        );
    }

    private List<java.util.Map<String, Object>> requestBatch(
            String stockCode,
            String fidInputDate1,
            String fidInputHour1
    ) throws IOException, InterruptedException {
        String url =
                properties.getBaseUrl()
                + "/uapi/domestic-stock/v1/quotations/inquire-time-dailychartprice"
                + "?FID_COND_MRKT_DIV_CODE=J"
                + "&FID_INPUT_ISCD=" + stockCode
                + "&FID_INPUT_DATE_1=" + fidInputDate1
                + "&FID_INPUT_HOUR_1=" + fidInputHour1
                + "&FID_PW_DATA_INCU_YN=Y"
                + "&FID_FAKE_TICK_INCU_YN=";

        return kisMinuteChartApiClient.fetch(url, TR_ID, stockCode, "과거분봉");
    }
}
