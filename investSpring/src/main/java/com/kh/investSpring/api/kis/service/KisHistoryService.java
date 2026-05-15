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

    private volatile boolean bulkSyncInProgress = false;
    private volatile boolean stopRequested = false;

    public boolean isBulkSyncInProgress() {
        return bulkSyncInProgress;
    }

    @Async
    public void syncAllHistory(AtomicBoolean running) {
        if (!running.compareAndSet(false, true)) {
            log.warn("이미 초기 적재 실행 중");
            return;
        }

        bulkSyncInProgress = true;
        stopRequested = false;

        try {
            List<String> stockCodes = stockHistoryDao.selectAllStockCodes();
            Map<String, Object> state = stockHistoryDao.selectFetchState();
            
            String lastStockCode = null;
            String lastPeriodType = null;

            if (state != null) {
                lastStockCode = (String) state.get("LAST_STOCK_CODE");
                lastPeriodType = (String) state.get("LAST_PERIOD_TYPE");
            }
            
            boolean resumeMode = lastStockCode != null;

            for (String stockCode : stockCodes) {
                if (stopRequested) {
                    log.info("중지 요청 감지 stockCode={}", stockCode);
                    break;
                }
                
                // resume skip
                if (resumeMode) {
                    if (!stockCode.equals(lastStockCode)) {
                        continue;
                    }
                    resumeMode = false;
                }
                
                try {
                	processPeriod(
                            stockCode,
                            "D",
                            lastStockCode,
                            lastPeriodType
                    );

                    processPeriod(
                            stockCode,
                            "W",
                            lastStockCode,
                            lastPeriodType
                    );

                    processPeriod(
                            stockCode,
                            "M",
                            lastStockCode,
                            lastPeriodType
                    );
                } catch (Exception e) {
                    log.error("과거시세 저장 실패 stockCode={}",
                            stockCode, e
                    );
                    stockHistoryDao.mergeFetchState(stockCode, "D");
                }
            }

            if (!stopRequested) {
                stockHistoryDao.clearFetchState();
                log.info("전체 종목 저장 완료");
            }
        } finally {
            bulkSyncInProgress = false;
            running.set(false);
            log.info("과거 시세 동기화 종료");
        }
    }
    
    private void processPeriod(
            String stockCode,
            String periodType,
            String lastStockCode,
            String lastPeriodType
    ) throws Exception {
        if (stopRequested) {
            stockHistoryDao.mergeFetchState(stockCode, periodType);

            throw new InterruptedException("중지 요청");
        }

        // resume period skip
        if (stockCode.equals(lastStockCode) && lastPeriodType != null) {
            if (lastPeriodType.equals("W") && periodType.equals("D")) {
                return;
            }

            if (lastPeriodType.equals("M") 
            		&& (periodType.equals("D")
            				|| periodType.equals("W"))) {
                return;
            }
        }

        syncHistory(stockCode, periodType);
        Thread.sleep(3000);

        // 다음 시작 위치 저장
        String nextPeriod = switch (periodType) {
            case "D" -> "W";
            case "W" -> "M";
            default -> "D";
        };

        stockHistoryDao.mergeFetchState(stockCode, nextPeriod);
    }

    public void syncHistory(String stockCode, String periodType) throws IOException, InterruptedException {
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
                        .header("content-type", "application/json; charset=utf-8")
                        .header("authorization", "Bearer " + token)
                        .header("appkey", properties.getAppKey())
                        .header("appsecret", properties.getAppSecret())
                        .header("tr_id", "FHKST01010400")
                        .GET()
                        .build();

        int retry = 0;

        while (true) {
            HttpResponse<String> response = httpClient.send(
						                            request,
						                            HttpResponse.BodyHandlers.ofString()
						                    );
            String bodyText = response.body();

            // HTTP 실패
            if (response.statusCode() != 200) {
                retry++;
                log.warn("HTTP 실패 stockCode={} period={} status={} retry={}",
                        stockCode,
                        periodType,
                        response.statusCode(),
                        retry
                );

                Thread.sleep(10000);
                continue;
            }

            // Rate Limit
            if (bodyText.contains("EGW00201")) {
                retry++;
                long wait = Math.min(60000, retry * 10000L);

                log.warn("Rate Limit stockCode={} period={} retry={} wait={}ms",
                        stockCode,
                        periodType,
                        retry,
                        wait
                );

                Thread.sleep(wait);
                continue;
            }

            Map<String, Object> body = objectMapper.readValue(
					                            bodyText,
					                            new TypeReference<Map<String, Object>>() {}
					                    );
            String rtCd = String.valueOf(body.get("rt_cd"));

            // API 실패
            if (!"0".equals(rtCd)) {
                retry++;
                log.warn("KIS API 실패 stockCode={} period={} rt_cd={} msg_cd={} msg={} retry={}",
                        stockCode,
                        periodType,
                        body.get("rt_cd"),
                        body.get("msg_cd"),
                        body.get("msg1"),
                        retry
                );

                Thread.sleep(10000);
                continue;
            }

            List<Map<String, Object>> output = (List<Map<String, Object>>) body.get("output");

            if (output == null || output.isEmpty()) {
                log.warn("데이터 없음 stockCode={} period={}",
                        stockCode,
                        periodType
                );

                return;
            }

            log.info("KIS 성공 stockCode={} period={} size={}",
                    stockCode,
                    periodType,
                    output.size()
            );

            for (Map<String, Object> item : output) {
                StockHistoryCacheDto dto = new StockHistoryCacheDto();

                dto.setStockCode(stockCode);
                dto.setPeriodType(periodType);
                dto.setBaseDate(
                        LocalDate.parse(
                                String.valueOf(item.get("stck_bsop_date")),
                                DateTimeFormatter.BASIC_ISO_DATE
                        )
                );
                dto.setOpenPrice(parseLong(item.get("stck_oprc")));
                dto.setHighPrice(parseLong(item.get("stck_hgpr")));
                dto.setLowPrice(parseLong(item.get("stck_lwpr")));
                dto.setClosePrice(parseLong(item.get("stck_clpr")));
                dto.setVolume(parseLong(item.get("acml_vol")));
                dto.setChangePrice(parseLong(item.get("prdy_vrss")));
                dto.setChangeRate(parseDouble(item.get("prdy_ctrt")));

                stockHistoryDao.mergeHistory(dto);
            }

            log.info("개별 시세 저장 완료 stockCode={} period={}",
                    stockCode,
                    periodType
            );

            return;
        }
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

    public void syncHistoryStop(AtomicBoolean historySyncStopRunning) {
        if (!historySyncStopRunning.compareAndSet(false, true)) {
            log.warn("이미 중지 요청 처리 중");
            return;
        }

        try {
            stopRequested = true;
            log.info("과거 시세 중지 요청 완료");
        } finally {
            historySyncStopRunning.set(false);
        }
    }
}