package com.kh.investSpring.api.kis.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.investSpring.api.kis.config.KisProperties;
import com.kh.investSpring.api.kis.dto.InvestorTrendDailyRow;
import com.kh.investSpring.api.kis.dto.InvestorTrendSummary;
import com.kh.investSpring.api.kis.dto.StockInvestorTrendResponse;
import com.kh.investSpring.api.kis.http.KisInquireInvestorHttpResponse;
import com.kh.investSpring.api.kis.http.KisInvestorTradeDailyHttpResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KisInvestorTradeService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private static final String DAILY_TR_ID = "FHPTJ04160001";
    private static final String DAILY_API_PATH =
            "/uapi/domestic-stock/v1/quotations/investor-trade-by-stock-daily";

    private static final String INQUIRE_INVESTOR_TR_ID = "FHKST01010900";
    private static final String INQUIRE_INVESTOR_API_PATH =
            "/uapi/domestic-stock/v1/quotations/inquire-investor";

    private static final long CACHE_TTL_MS = 300_000L;
    private static final int ANCHOR_LOOKBACK_DAYS = 25;

    private static final String AFTER_HOURS_NOTICE =
            "투자자 매매동향(일별) API는 15:40 이후 호출이 제한됩니다. 15:40 이전에 조회한 데이터가 있으면 표시됩니다.";
    private static final String NO_DATA_NOTICE =
            "매매동향 데이터를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.";
    private static final String STALE_NOTICE =
            "최근 조회한 매매동향 데이터를 표시합니다. 최신 데이터는 00:00~15:40에 갱신됩니다.";
    private static final String FALLBACK_NOTICE =
            "일별 API 제한으로 현재가 투자자 API 데이터를 표시합니다.";

    private static final int DEFAULT_DAYS = 30;
    private static final int MAX_DAYS = 60;
    private static final int MAX_PAGINATION_DEPTH = 10;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RestClient restClient;
    private final KisProperties kisProperties;
    private final KisTokenService kisTokenService;
    private final KisApiRequestCoordinator kisApiRequestCoordinator;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, CachedValue<StockInvestorTrendResponse>> cache =
            new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CachedValue<StockInvestorTrendResponse>> staleCache =
            new ConcurrentHashMap<>();

    public KisInvestorTradeService(
            RestClient restClient,
            KisProperties kisProperties,
            KisTokenService kisTokenService,
            KisApiRequestCoordinator kisApiRequestCoordinator,
            ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.kisProperties = kisProperties;
        this.kisTokenService = kisTokenService;
        this.kisApiRequestCoordinator = kisApiRequestCoordinator;
        this.objectMapper = objectMapper;
    }

    public StockInvestorTrendResponse getInvestorTrend(String stockCode, Integer days) {
        String code = normalizeStockCode(stockCode);
        int requestedDays = normalizeDays(days);
        String cacheKey = code + ":" + requestedDays;

        StockInvestorTrendResponse cached = getCached(cacheKey);
        if (cached != null) {
            return cached;
        }

        StockInvestorTrendResponse response = fetchWithFallbacks(code, requestedDays, cacheKey);
        putCached(cacheKey, response);
        return response;
    }

    private StockInvestorTrendResponse fetchWithFallbacks(String stockCode, int days, String cacheKey) {
        StockInvestorTrendResponse daily = tryFetchDaily(stockCode, days);
        if (daily != null && !daily.rows().isEmpty()) {
            return daily;
        }

        StockInvestorTrendResponse inquire = tryFetchInquireInvestor(stockCode, days);
        if (inquire != null && !inquire.rows().isEmpty()) {
            return inquire;
        }

        StockInvestorTrendResponse stale = getStale(cacheKey);
        if (stale != null && !stale.rows().isEmpty()) {
            return withNotice(stale, STALE_NOTICE);
        }

        String notice = isOutsideDailyApiWindow() ? AFTER_HOURS_NOTICE : NO_DATA_NOTICE;
        return new StockInvestorTrendResponse(stockCode, null, List.of(), notice);
    }

    private StockInvestorTrendResponse tryFetchDaily(String stockCode, int days) {
        InvestorTrendTimeLimitException lastTimeLimit = null;

        for (int dayOffset = 0; dayOffset <= ANCHOR_LOOKBACK_DAYS; dayOffset++) {
            String anchorDate = LocalDate.now(KST).minusDays(dayOffset).format(DATE_FMT);

            try {
                StockInvestorTrendResponse response = fetchDailyWithAnchor(stockCode, days, anchorDate);
                if (!response.rows().isEmpty()) {
                    if (dayOffset > 0) {
                        log.info(
                                "투자자매매동향 일별: anchor {} 일자로 {}건 조회 (stockCode={})",
                                anchorDate,
                                response.rows().size(),
                                stockCode);
                    }
                    return response;
                }
            } catch (InvestorTrendTimeLimitException exception) {
                lastTimeLimit = exception;
                log.debug("투자자매매동향 일별 TIME LIMIT (anchor={}, stockCode={})", anchorDate, stockCode);
            } catch (IllegalStateException exception) {
                log.warn(
                        "투자자매매동향 일별 조회 실패 (anchor={}, stockCode={}): {}",
                        anchorDate,
                        stockCode,
                        exception.getMessage());
            }
        }

        if (lastTimeLimit != null) {
            log.info("투자자매매동향 일별 API 시간 제한 — inquire-investor 폴백 시도 (stockCode={})", stockCode);
        }

        return null;
    }

    private StockInvestorTrendResponse tryFetchInquireInvestor(String stockCode, int days) {
        try {
            String accessToken = kisTokenService.getAccessToken();
            String url = kisProperties.getBaseUrl()
                    + INQUIRE_INVESTOR_API_PATH
                    + "?FID_COND_MRKT_DIV_CODE=J"
                    + "&FID_INPUT_ISCD=" + stockCode;

            KisInquireInvestorHttpResponse body = kisApiRequestCoordinator.execute(
                    () -> restClient.get()
                            .uri(url)
                            .headers(headers -> addKisHeaders(headers, INQUIRE_INVESTOR_TR_ID, accessToken))
                            .retrieve()
                            .body(KisInquireInvestorHttpResponse.class));

            if (body == null) {
                log.warn("inquire-investor 응답 없음 (stockCode={})", stockCode);
                return null;
            }

            if (!"0".equals(body.rt_cd())) {
                log.warn(
                        "inquire-investor 실패 (stockCode={}): {}",
                        stockCode,
                        body.msg1());
                return null;
            }

            List<InvestorTrendDailyRow> rows = mapOutputRows(body.output()).stream()
                    .sorted(Comparator.comparing(InvestorTrendDailyRow::tradeDate).reversed())
                    .limit(days)
                    .toList();

            if (rows.isEmpty()) {
                return null;
            }

            InvestorTrendSummary summary = new InvestorTrendSummary(
                    rows.get(0).tradeDate(),
                    rows.get(0).individualNetQty(),
                    rows.get(0).foreignNetQty(),
                    rows.get(0).institutionNetQty());

            return new StockInvestorTrendResponse(stockCode, summary, rows, FALLBACK_NOTICE);
        } catch (RuntimeException exception) {
            log.warn("inquire-investor 호출 예외 (stockCode={}): {}", stockCode, exception.getMessage());
            return null;
        }
    }

    private StockInvestorTrendResponse fetchDailyWithAnchor(String stockCode, int days, String anchorDate) {
        Map<String, InvestorTrendDailyRow> rowByDate = new LinkedHashMap<>();
        boolean continuation = false;
        int depth = 0;

        while (depth < MAX_PAGINATION_DEPTH) {
            final boolean nextPage = continuation;
            PageResult page = kisApiRequestCoordinator.execute(
                    () -> fetchDailyPage(stockCode, anchorDate, nextPage));

            mergeRows(rowByDate, page.rows());

            if (rowByDate.size() >= days || !page.hasNext()) {
                break;
            }

            continuation = true;
            depth++;
        }

        List<InvestorTrendDailyRow> sorted = rowByDate.values().stream()
                .sorted(Comparator.comparing(InvestorTrendDailyRow::tradeDate).reversed())
                .limit(days)
                .toList();

        InvestorTrendSummary summary = sorted.isEmpty()
                ? null
                : new InvestorTrendSummary(
                        sorted.get(0).tradeDate(),
                        sorted.get(0).individualNetQty(),
                        sorted.get(0).foreignNetQty(),
                        sorted.get(0).institutionNetQty());

        return new StockInvestorTrendResponse(stockCode, summary, sorted);
    }

    private PageResult fetchDailyPage(String stockCode, String anchorDate, boolean continuation) {
        String accessToken = kisTokenService.getAccessToken();
        String url = kisProperties.getBaseUrl()
                + DAILY_API_PATH
                + "?FID_COND_MRKT_DIV_CODE=J"
                + "&FID_INPUT_ISCD=" + stockCode
                + "&FID_INPUT_DATE_1=" + anchorDate
                + "&FID_ORG_ADJ_PRC="
                + "&FID_ETC_CLS_CODE=";

        ResponseEntity<KisInvestorTradeDailyHttpResponse> entity = restClient.get()
                .uri(url)
                .headers(headers -> {
                    addKisHeaders(headers, DAILY_TR_ID, accessToken);
                    if (continuation) {
                        headers.set("tr_cont", "N");
                    }
                })
                .retrieve()
                .toEntity(KisInvestorTradeDailyHttpResponse.class);

        KisInvestorTradeDailyHttpResponse body = entity.getBody();
        if (body == null) {
            throw new IllegalStateException("한국투자증권 투자자매매동향 응답이 없습니다.");
        }

        if (!"0".equals(body.rt_cd())) {
            String message = body.msg1() == null ? "" : body.msg1();
            if (isInvestorTrendTimeLimit(message)) {
                throw new InvestorTrendTimeLimitException(message);
            }
            throw new IllegalStateException("한국투자증권 투자자매매동향 조회 실패: " + message);
        }

        List<InvestorTrendDailyRow> rows = new ArrayList<>();
        rows.addAll(mapOutputRows(body.output1()));
        rows.addAll(mapOutputRows(body.output2()));

        String trCont = entity.getHeaders().getFirst("tr_cont");
        boolean hasNext = "M".equals(trCont) || "F".equals(trCont);

        return new PageResult(rows, hasNext);
    }

    private List<InvestorTrendDailyRow> mapOutputRows(Object output) {
        List<Map<String, Object>> maps = toMapList(output);
        List<InvestorTrendDailyRow> rows = new ArrayList<>();

        for (Map<String, Object> map : maps) {
            String tradeDate = valueToString(map.get("stck_bsop_date"));
            if (tradeDate == null || tradeDate.isBlank()) {
                continue;
            }

            Long individual = firstLongValue(map, "prsn_ntby_qty", "prsn_ntby_tr_pbmn");
            Long foreign = firstLongValue(map, "frgn_ntby_qty", "frgn_ntby_tr_pbmn");
            Long institution = firstLongValue(map, "orgn_ntby_qty", "orgn_ntby_tr_pbmn");

            if (individual == null && foreign == null && institution == null) {
                continue;
            }

            rows.add(new InvestorTrendDailyRow(tradeDate, individual, foreign, institution));
        }

        return rows;
    }

    private Long firstLongValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Long value = parseLongValue(map.get(key));
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private void mergeRows(
            Map<String, InvestorTrendDailyRow> rowByDate,
            List<InvestorTrendDailyRow> rows) {
        for (InvestorTrendDailyRow row : rows) {
            rowByDate.putIfAbsent(row.tradeDate(), row);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> toMapList(Object output) {
        if (output == null) {
            return Collections.emptyList();
        }

        if (output instanceof List<?> list) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    result.add((Map<String, Object>) map);
                } else {
                    result.add(objectMapper.convertValue(item, new TypeReference<Map<String, Object>>() {}));
                }
            }
            return result;
        }

        if (output instanceof Map<?, ?> map) {
            return List.of((Map<String, Object>) map);
        }

        return List.of(objectMapper.convertValue(output, new TypeReference<Map<String, Object>>() {}));
    }

    private static Long parseLongValue(Object value) {
        if (value == null) {
            return null;
        }

        String text = String.valueOf(value).trim().replace(",", "");
        if (text.isEmpty()) {
            return null;
        }

        try {
            return Long.parseLong(text);
        } catch (NumberFormatException ignored) {
            try {
                return (long) Double.parseDouble(text);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    private static String valueToString(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private static String normalizeStockCode(String stockCode) {
        return stockCode == null ? "" : stockCode.trim();
    }

    private static int normalizeDays(Integer days) {
        if (days == null || days < 1) {
            return DEFAULT_DAYS;
        }
        return Math.min(days, MAX_DAYS);
    }

    private static boolean isOutsideDailyApiWindow() {
        ZonedDateTime now = ZonedDateTime.now(KST);
        int minutes = now.getHour() * 60 + now.getMinute();
        return minutes > 15 * 60 + 40;
    }

    private void addKisHeaders(
            org.springframework.http.HttpHeaders headers,
            String trId,
            String bearerAccessToken) {
        headers.set("content-type", "application/json; charset=utf-8");
        headers.set("authorization", "Bearer " + bearerAccessToken);
        headers.set("appkey", kisProperties.getAppKey());
        headers.set("appsecret", kisProperties.getAppSecret());
        headers.set("tr_id", trId);
    }

    private StockInvestorTrendResponse getCached(String cacheKey) {
        CachedValue<StockInvestorTrendResponse> cached = cache.get(cacheKey);
        if (cached == null) {
            return null;
        }

        if (System.currentTimeMillis() - cached.createdAtMillis() > CACHE_TTL_MS) {
            cache.remove(cacheKey, cached);
            return null;
        }

        return cached.value();
    }

    private void putCached(String cacheKey, StockInvestorTrendResponse value) {
        if (value == null || value.rows() == null || value.rows().isEmpty()) {
            return;
        }

        long createdAtMillis = System.currentTimeMillis();
        StockInvestorTrendResponse withoutNotice =
                new StockInvestorTrendResponse(
                        value.stockCode(), value.summary(), value.rows());
        cache.put(cacheKey, new CachedValue<>(withoutNotice, createdAtMillis));
        staleCache.put(cacheKey, new CachedValue<>(withoutNotice, createdAtMillis));
    }

    private StockInvestorTrendResponse getStale(String cacheKey) {
        CachedValue<StockInvestorTrendResponse> cached = staleCache.get(cacheKey);
        return cached == null ? null : cached.value();
    }

    private static boolean isInvestorTrendTimeLimit(String message) {
        return message != null && message.toUpperCase().contains("TIME LIMIT");
    }

    private static StockInvestorTrendResponse withNotice(
            StockInvestorTrendResponse source,
            String notice) {
        return new StockInvestorTrendResponse(
                source.stockCode(), source.summary(), source.rows(), notice);
    }

    private static final class InvestorTrendTimeLimitException extends RuntimeException {
        private InvestorTrendTimeLimitException(String message) {
            super(message);
        }
    }

    private record PageResult(List<InvestorTrendDailyRow> rows, boolean hasNext) {
    }

    private record CachedValue<T>(T value, long createdAtMillis) {
    }
}
