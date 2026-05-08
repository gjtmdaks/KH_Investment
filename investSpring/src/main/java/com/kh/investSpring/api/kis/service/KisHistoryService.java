package com.kh.investSpring.api.kis.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.investSpring.api.kis.config.KisProperties;
import com.kh.investSpring.api.kis.dao.StockHistoryDao;
import com.kh.investSpring.api.kis.dto.StockHistoryCacheDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KisHistoryService {

    private final KisProperties properties;
    private final KisTokenService kisTokenService;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final StockHistoryDao stockHistoryDao;

    @Async
    public void syncAllHistory(AtomicBoolean running) {

        if (!running.compareAndSet(false, true)) {
            log.warn("이미 초기 적재 실행 중");
            return;
        }
        
        try {
            List<String> stockCodes = stockHistoryDao.selectAllStockCodes();

            for (String stockCode : stockCodes) {
                try {
                    syncHistory(stockCode, "D");
                    Thread.sleep(1000);

                    syncHistory(stockCode, "W");
                    Thread.sleep(1000);

                    syncHistory(stockCode, "M");
                    Thread.sleep(1000);
                } catch (Exception e) {
                    log.error("과거시세 저장 실패 stockCode={}",
                            stockCode,
                            e
                    );
                }
            }

            log.info("과거 시세 저장 완료");
        } finally {
            running.set(false);
            log.info("과거 시세 동기화 락 해제");
        }
    }

    public void syncHistory(
            String stockCode,
            String periodType
    ) throws IOException, InterruptedException {

        String token = kisTokenService.getAccessToken();

        String url =
                properties.getBaseUrl()
                + "/uapi/domestic-stock/v1/quotations/inquire-daily-price"
                + "?FID_COND_MRKT_DIV_CODE=UN"
                + "&FID_INPUT_ISCD=" + stockCode
                + "&FID_PERIOD_DIV_CODE=" + periodType
                + "&FID_ORG_ADJ_PRC=1";

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header(
                                "content-type",
                                "application/json; charset=utf-8"
                        )
                        .header(
                                "authorization",
                                "Bearer " + token
                        )
                        .header(
                                "appkey",
                                properties.getAppKey()
                        )
                        .header(
                                "appsecret",
                                properties.getAppSecret()
                        )
                        .header(
                                "tr_id",
                                "FHKST01010400"
                        )
                        .GET()
                        .build();

        int retry = 0;
        HttpResponse<String> response = null;

        while (retry < 5) {

            response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.body().contains("EGW00201")) {

                retry++;

                log.warn(
                        "Rate Limit 발생 stockCode={} period={} retry={}",
                        stockCode,
                        periodType,
                        retry
                );

                Thread.sleep(5000);

                continue;
            }

            if (response.statusCode() != 200) {

                log.warn(
                        "KIS 응답 실패 stockCode={} status={} body={}",
                        stockCode,
                        response.statusCode(),
                        response.body()
                );

                return;
            }

            break;
        }
        if (retry >= 5) {

            log.warn(
                    "Rate Limit 최대 재시도 초과 stockCode={} period={}",
                    stockCode,
                    periodType
            );

            return;
        }
        if (response == null) {
            return;
        }

        Map<String, Object> body =
                objectMapper.readValue(
                        response.body(),
                        new TypeReference<Map<String, Object>>() {}
                );

        if (!"0".equals(String.valueOf(body.get("rt_cd")))) {

            log.warn(
                    "KIS API 실패 stockCode={} msg={}",
                    stockCode,
                    body.get("msg1")
            );

            return;
        }

        List<Map<String, Object>> output =
                (List<Map<String, Object>>) body.get("output");

        if (output == null || output.isEmpty()) {
            return;
        }

        for (Map<String, Object> item : output) {

            try {

                StockHistoryCacheDto dto =
                        new StockHistoryCacheDto();

                dto.setStockCode(stockCode);
                dto.setPeriodType(periodType);

                dto.setBaseDate(
                        LocalDate.parse(
                                String.valueOf(
                                        item.get("stck_bsop_date")
                                ),
                                DateTimeFormatter.BASIC_ISO_DATE
                        )
                );

                dto.setOpenPrice(
                        parseLong(item.get("stck_oprc"))
                );

                dto.setHighPrice(
                        parseLong(item.get("stck_hgpr"))
                );

                dto.setLowPrice(
                        parseLong(item.get("stck_lwpr"))
                );

                dto.setClosePrice(
                        parseLong(item.get("stck_clpr"))
                );

                dto.setVolume(
                        parseLong(item.get("acml_vol"))
                );

                dto.setChangePrice(
                        parseLong(item.get("prdy_vrss"))
                );

                dto.setChangeRate(
                        parseDouble(item.get("prdy_ctrt"))
                );

                stockHistoryDao.mergeHistory(dto);

            } catch (Exception e) {

                log.error(
                        "개별 시세 저장 실패 stockCode={} period={}",
                        stockCode,
                        periodType,
                        e
                );
            }
        }

        log.info(
                "개별 시세 저장 완료 stockCode={} period={}",
                stockCode,
                periodType
        );
    }

    private Long parseLong(Object value) {

        if (value == null) {
            return 0L;
        }

        try {

            return Long.parseLong(
                    String.valueOf(value)
                            .replace(",", "")
                            .trim()
            );

        } catch (Exception e) {
            return 0L;
        }
    }

    private Double parseDouble(Object value) {

        if (value == null) {
            return 0.0;
        }

        try {

            return Double.parseDouble(
                    String.valueOf(value)
                            .replace(",", "")
                            .trim()
            );

        } catch (Exception e) {
            return 0.0;
        }
    }
}