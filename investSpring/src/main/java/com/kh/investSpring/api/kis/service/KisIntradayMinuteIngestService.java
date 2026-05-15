package com.kh.investSpring.api.kis.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private static final DateTimeFormatter BASIC_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    private final KisProperties properties;
    private final KisTokenService kisTokenService;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
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

        TreeMap<String, StockIntradayMinuteCacheDto> merged = new TreeMap<>();

        String cursor = initialCursorHHMMSS(tradeDate);
        int iterations = 0;

        while (iterations++ < 96) {
            List<Map<String, Object>> batch = requestBatch(stockCode, cursor);

            if (batch.isEmpty()) {
                String prev = minusOneMinute(cursor);

                if (prev.compareTo("085959") <= 0) {
                    break;
                }

                cursor = prev;
                Thread.sleep(280);

                continue;
            }

            int sizeBefore = merged.size();

            for (Map<String, Object> row : batch) {
                StockIntradayMinuteCacheDto dto = toDto(stockCode, row);

                if (dto == null || dto.getTradeDate() == null || dto.getBarTime() == null) {
                    continue;
                }

                if (!dto.getTradeDate().equals(tradeDate)) {
                    continue;
                }

                merged.put(dto.getBarTime(), dto);
            }

            String minTime = batch.stream()
                    .map(row -> normalizeHhmmss(readCntgHour(row)))
                    .filter(s -> s != null && s.length() == 6)
                    .min(Comparator.naturalOrder())
                    .orElse(null);

            if (minTime == null) {
                break;
            }

            if (merged.size() == sizeBefore) {
                String prev = minusOneMinute(cursor);

                if (prev.compareTo(minTime) >= 0) {
                    cursor = prev;
                } else {
                    cursor = minusOneMinute(minTime);
                }
            } else {
                cursor = minusOneMinute(minTime);
            }

            if (minTime.compareTo("090000") <= 0) {
                break;
            }

            Thread.sleep(320);
        }

        List<StockIntradayMinuteCacheDto> ordered = new ArrayList<>(merged.values());
        ordered.sort(Comparator.comparing(StockIntradayMinuteCacheDto::getBarTime));

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

    private List<Map<String, Object>> requestBatch(String stockCode, String fidInputHour1)
            throws IOException, InterruptedException {
        String url =
                properties.getBaseUrl()
                + "/uapi/domestic-stock/v1/quotations/inquire-time-itemchartprice"
                + "?FID_COND_MRKT_DIV_CODE=J"
                + "&FID_INPUT_ISCD=" + stockCode
                + "&FID_INPUT_HOUR_1=" + fidInputHour1
                + "&FID_ETC_CLS_CODE="
                + "&FID_PW_DATA_INCU_YN=Y";

        int retry = 0;

        while (true) {
            String token = kisTokenService.getAccessToken();
            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("content-type", "application/json; charset=utf-8")
                            .header("authorization", "Bearer " + token)
                            .header("appkey", properties.getAppKey())
                            .header("appsecret", properties.getAppSecret())
                            .header("tr_id", TR_ID)
                            .GET()
                            .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );
            String bodyText = response.body();

            if (response.statusCode() != 200) {
                retry++;
                log.warn(
                        "분봉 HTTP 실패 stockCode={} status={} retry={}",
                        stockCode,
                        response.statusCode(),
                        retry
                );

                Thread.sleep(10_000L);

                continue;
            }

            if (bodyText.contains("EGW00201")) {
                retry++;
                long wait = Math.min(60_000L, retry * 10_000L);

                log.warn("분봉 rate limit stockCode={} retry={} wait={}ms", stockCode, retry, wait);

                Thread.sleep(wait);

                continue;
            }

            Map<String, Object> body = objectMapper.readValue(
                    bodyText,
                    new TypeReference<Map<String, Object>>() {}
            );
            String rtCd = String.valueOf(body.get("rt_cd"));

            if (!"0".equals(rtCd)) {
                retry++;

                log.warn(
                        "분봉 KIS 실패 stockCode={} rt_cd={} msg={} retry={}",
                        stockCode,
                        body.get("rt_cd"),
                        body.get("msg1"),
                        retry
                );

                Thread.sleep(10_000L);

                continue;
            }

            Object out2 = extractOutput2(body);

            if (!(out2 instanceof List<?> rawList)) {
                return List.of();
            }

            List<Map<String, Object>> list = new ArrayList<>();

            for (Object item : rawList) {
                if (item instanceof Map<?, ?> m) {
                    Map<String, Object> cast = new LinkedHashMap<>();

                    for (Map.Entry<?, ?> e : m.entrySet()) {
                        cast.put(String.valueOf(e.getKey()), e.getValue());
                    }

                    list.add(cast);
                }
            }

            return list;
        }
    }

    private static Object extractOutput2(Map<String, Object> body) {
        Object out2 = body.get("output2");

        if (out2 != null) {
            return out2;
        }

        Object output = body.get("output");

        if (output instanceof List<?> list && !list.isEmpty()) {
            Object first = list.get(0);

            if (first instanceof Map<?, ?> row && row.containsKey("stck_cntg_hour")) {
                return output;
            }
        }

        return null;
    }

    private StockIntradayMinuteCacheDto toDto(String stockCode, Map<String, Object> item) {
        LocalDate tradeDate = parseTradeDate(item.get("stck_bsop_date"));

        if (tradeDate == null) {
            return null;
        }

        String barTime = normalizeHhmmss(readCntgHour(item));

        if (barTime == null) {
            return null;
        }

        StockIntradayMinuteCacheDto dto = new StockIntradayMinuteCacheDto();
        dto.setStockCode(stockCode);
        dto.setTradeDate(tradeDate);
        dto.setBarTime(barTime);
        dto.setOpenPrice(parseLong(item.get("stck_oprc")));
        dto.setHighPrice(parseLong(item.get("stck_hgpr")));
        dto.setLowPrice(parseLong(item.get("stck_lwpr")));
        dto.setClosePrice(parseLong(item.get("stck_clpr")));
        dto.setVolume(
                firstPositiveLong(
                        item.get("cntg_vol"),
                        item.get("acml_vol")
                )
        );

        return dto;
    }

    private static String readCntgHour(Map<String, Object> item) {
        Object v = item.get("stck_cntg_hour");

        return v == null ? null : String.valueOf(v).trim();
    }

    private LocalDate parseTradeDate(Object raw) {
        if (raw == null) {
            return null;
        }

        String s = String.valueOf(raw).trim();

        if (s.length() != 8 || !s.chars().allMatch(Character::isDigit)) {
            return null;
        }

        return LocalDate.parse(s, BASIC_DATE);
    }

    private static String normalizeHhmmss(String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }

        String digits = raw.replace(":", "").trim();

        if (digits.length() >= 6) {
            return digits.substring(digits.length() - 6);
        }

        return "0".repeat(6 - digits.length()) + digits;
    }

    private static String minusOneMinute(String hhmmss) {
        int h = Integer.parseInt(hhmmss.substring(0, 2));
        int m = Integer.parseInt(hhmmss.substring(2, 4));
        int s = Integer.parseInt(hhmmss.substring(4, 6));
        LocalTime t = LocalTime.of(h, m, s).minusMinutes(1);

        return String.format("%02d%02d%02d", t.getHour(), t.getMinute(), t.getSecond());
    }

    private static long parseLong(Object v) {
        if (v == null) {
            return 0L;
        }

        try {
            return Long.parseLong(
                    String.valueOf(v)
                            .replace(",", "")
                            .trim()
            );
        } catch (Exception e) {
            return 0L;
        }
    }

    private static long firstPositiveLong(Object primary, Object secondary) {
        long a = parseLong(primary);

        if (a > 0L) {
            return a;
        }

        return parseLong(secondary);
    }
}
