package com.kh.investSpring.api.kis.service;

import java.time.LocalDate;
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
import com.kh.investSpring.api.kis.http.KisInvestorTradeDailyHttpResponse;

@Service
public class KisInvestorTradeService {

    private static final String TR_ID = "FHPTJ04160001";
    private static final String API_PATH =
            "/uapi/domestic-stock/v1/quotations/investor-trade-by-stock-daily";
    private static final long CACHE_TTL_MS = 60_000L;
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

        StockInvestorTrendResponse response = fetchFromKis(code, requestedDays);
        putCached(cacheKey, response);
        return response;
    }

    private StockInvestorTrendResponse fetchFromKis(String stockCode, int days) {
        String anchorDate = LocalDate.now().format(DATE_FMT);
        Map<String, InvestorTrendDailyRow> rowByDate = new LinkedHashMap<>();
        boolean continuation = false;
        int depth = 0;

        while (depth < MAX_PAGINATION_DEPTH) {
            final boolean nextPage = continuation;
            PageResult page = kisApiRequestCoordinator.execute(
                    () -> fetchPage(stockCode, anchorDate, nextPage));

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

    private PageResult fetchPage(String stockCode, String anchorDate, boolean continuation) {
        String accessToken = kisTokenService.getAccessToken();
        String url = kisProperties.getBaseUrl()
                + API_PATH
                + "?FID_COND_MRKT_DIV_CODE=J"
                + "&FID_INPUT_ISCD=" + stockCode
                + "&FID_INPUT_DATE_1=" + anchorDate
                + "&FID_ORG_ADJ_PRC="
                + "&FID_ETC_CLS_CODE=";

        ResponseEntity<KisInvestorTradeDailyHttpResponse> entity = restClient.get()
                .uri(url)
                .headers(headers -> {
                    addKisHeaders(headers, TR_ID, accessToken);
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
            throw new IllegalStateException("한국투자증권 투자자매매동향 조회 실패: " + body.msg1());
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

            Long individual = parseLongValue(map.get("prsn_ntby_qty"));
            Long foreign = parseLongValue(map.get("frgn_ntby_qty"));
            Long institution = parseLongValue(map.get("orgn_ntby_qty"));

            if (individual == null && foreign == null && institution == null) {
                continue;
            }

            rows.add(new InvestorTrendDailyRow(tradeDate, individual, foreign, institution));
        }

        return rows;
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
        if (value == null) {
            return;
        }
        cache.put(cacheKey, new CachedValue<>(value, System.currentTimeMillis()));
    }

    private record PageResult(List<InvestorTrendDailyRow> rows, boolean hasNext) {
    }

    private record CachedValue<T>(T value, long createdAtMillis) {
    }
}
