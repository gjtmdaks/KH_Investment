package com.kh.investSpring.api.kis.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.kh.investSpring.api.dart.service.StockStaticProfileService;
import com.kh.investSpring.api.kis.config.KisProperties;
import com.kh.investSpring.api.kis.dto.KisStockDetailResponse;
import com.kh.investSpring.api.kis.dto.KisStockOrderbookResponse;
import com.kh.investSpring.api.kis.dto.KisStockPriceResponse;
import com.kh.investSpring.api.kis.dto.KisStockSummaryResponse;
import com.kh.investSpring.domain.stock.dao.StockDao;
import com.kh.investSpring.domain.stock.dto.StockInfoDto;

@Service
public class KisStockService {

    private static final long PRICE_CACHE_TTL_MS = 1_000L;
    private static final long ORDERBOOK_CACHE_TTL_MS = 1_000L;
    private static final String BATCH_CHG_CACHE_PREFIX = "invest:kis:batch:chg:";
    private static final Duration BATCH_CHG_TTL = Duration.ofSeconds(45);
    private final RestClient restClient;
    private final KisProperties kisProperties;
    private final KisTokenService kisTokenService;
    private final KisApiRequestCoordinator kisApiRequestCoordinator;
    private final StockDao stockDao;
    private final StockStaticProfileService stockStaticProfileService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, CachedValue<KisStockPriceResponse>> priceCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CachedValue<KisStockOrderbookResponse>> orderbookCache = new ConcurrentHashMap<>();

    public KisStockService(
            RestClient restClient,
            KisProperties kisProperties,
            KisTokenService kisTokenService,
            KisApiRequestCoordinator kisApiRequestCoordinator,
            StockDao stockDao,
            StockStaticProfileService stockStaticProfileService,
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.kisProperties = kisProperties;
        this.kisTokenService = kisTokenService;
        this.kisApiRequestCoordinator = kisApiRequestCoordinator;
        this.stockDao = stockDao;
        this.stockStaticProfileService = stockStaticProfileService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public KisStockPriceResponse getStockPrice(String stockCode) {
        KisStockPriceResponse cached = getCached(priceCache, stockCode, PRICE_CACHE_TTL_MS);
        if (cached != null) {
            return cached;
        }
        String accessToken = kisTokenService.getAccessToken();

        String url = kisProperties.getBaseUrl()
                + "/uapi/domestic-stock/v1/quotations/inquire-price"
                + "?FID_COND_MRKT_DIV_CODE=J"
                + "&FID_INPUT_ISCD=" + stockCode;

        KisInquirePriceResponse response = kisApiRequestCoordinator.execute(
                () -> restClient.get()
                        .uri(url)
                        .header("content-type", "application/json; charset=utf-8")
                        .header("authorization", "Bearer " + accessToken)
                        .header("appkey", kisProperties.getAppKey())
                        .header("appsecret", kisProperties.getAppSecret())
                        .header("tr_id", "FHKST01010100")
                        .retrieve()
                        .body(KisInquirePriceResponse.class));

        if (response == null) {
            throw new IllegalStateException("한국투자증권 현재가 응답이 없습니다.");
        }

        if (!"0".equals(response.rt_cd())) {
            throw new IllegalStateException("한국투자증권 현재가 조회 실패: " + response.msg1());
        }

        Map<String, Object> output = safeMap(response.output());

        KisStockPriceResponse result = new KisStockPriceResponse(
                stockCode,
                valueToString(output.get("hts_kor_isnm")),
                valueToString(output.get("stck_prpr")),
                valueToString(output.get("prdy_vrss")),
                valueToString(output.get("prdy_ctrt")),
                valueToString(output.get("acml_vol")),
                valueToString(output.get("acml_tr_pbmn")),
                valueToString(output.get("stck_oprc")),
                valueToString(output.get("stck_hgpr")),
                valueToString(output.get("stck_lwpr")));
        putCached(priceCache, stockCode, result);
        return result;
    }

    /**
     * 뉴스 관련 종목 칩 등: 한 번의 HTTP로 여러 종목 등락률(prdy_ctrt)을 채울 때 사용.
     * 종목마다 기존 {@link #getStockPrice(String)}를 호출하며 1초 메모리 캐시를 공유한다.
     */
    public Map<String, String> getChangeRatesByStockCodes(List<String> rawCodes) {
        if (rawCodes == null || rawCodes.isEmpty()) {
            return Map.of();
        }
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String c : rawCodes) {
            if (c != null && !c.isBlank()) {
                unique.add(c.trim());
            }
        }
        if (unique.isEmpty()) {
            return Map.of();
        }
        if (unique.size() > 100) {
            throw new IllegalArgumentException("한 번에 최대 100개 종목코드까지 조회할 수 있습니다.");
        }
        List<String> sorted = new ArrayList<>(unique);
        Collections.sort(sorted);
        String cacheKey = BATCH_CHG_CACHE_PREFIX + fingerprintSortedCodes(sorted);
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null && !cached.isBlank()) {
                Map<String, String> parsed = objectMapper.readValue(cached, new TypeReference<Map<String, String>>() {
                });
                if (parsed != null && !parsed.isEmpty()) {
                    return new LinkedHashMap<>(parsed);
                }
            }
        } catch (Exception ignored) {
            // best-effort: miss and fetch from KIS
        }

        Map<String, String> fresh = fetchChangeRatesFromKis(unique);
        try {
            stringRedisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(fresh), BATCH_CHG_TTL);
        } catch (JsonProcessingException ignored) {
            // skip cache write
        }
        return fresh;
    }

    private Map<String, String> fetchChangeRatesFromKis(LinkedHashSet<String> unique) {
        Map<String, String> out = new LinkedHashMap<>();
        for (String code : unique) {
            try {
                KisStockPriceResponse p = getStockPrice(code);
                String rate = p.changeRate();
                if (rate != null && !rate.isBlank()) {
                    out.put(code, rate);
                } else {
                    out.put(code, null);
                }
            } catch (Exception e) {
                out.put(code, null);
            }
        }
        return out;
    }

    private static String fingerprintSortedCodes(List<String> sortedCodes) {
        String payload = String.join(",", sortedCodes);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(payload.hashCode());
        }
    }

    public KisStockOrderbookResponse getStockOrderbook(String stockCode) {
        KisStockOrderbookResponse cached = getCached(orderbookCache, stockCode, ORDERBOOK_CACHE_TTL_MS);
        if (cached != null) {
            return cached;
        }

        String bearer = kisTokenService.getAccessToken();

        String url = kisProperties.getBaseUrl()
                + "/uapi/domestic-stock/v1/quotations/inquire-asking-price-exp-ccn"
                + "?FID_COND_MRKT_DIV_CODE=J"
                + "&FID_INPUT_ISCD=" + stockCode;

        KisOrderbookApiResponse response = kisApiRequestCoordinator.execute(
                () -> restClient.get()
                        .uri(url)
                        .headers(headers -> addKisHeaders(headers, "FHKST01010200", bearer))
                        .retrieve()
                        .body(KisOrderbookApiResponse.class));

        if (response == null) {
            throw new IllegalStateException("한국투자증권 호가 응답이 없습니다.");
        }

        if (!"0".equals(response.rt_cd())) {
            throw new IllegalStateException("한국투자증권 호가 조회 실패: " + response.msg1());
        }

        Map<String, Object> output1 = safeMap(response.output1());
        Map<String, Object> output2 = safeMap(response.output2());

        KisStockOrderbookResponse result = new KisStockOrderbookResponse(
                stockCode,
                buildOrderbookLevels(output1, "askp", "askp_rsqn", "askp_rsqn_icdc"),
                buildOrderbookLevels(output1, "bidp", "bidp_rsqn", "bidp_rsqn_icdc"),
                valueToString(output1.get("total_askp_rsqn")),
                valueToString(output1.get("total_bidp_rsqn")),
                valueToString(output2.get("stck_prpr")),
                valueToString(output2.get("cntg_vol")));
        putCached(orderbookCache, stockCode, result);
        return result;
    }

    public KisStockSummaryResponse getStockSummary(String stockCode) {
        if (kisProperties.isVirtualTrading()) {
            return buildLocalStockSummary(stockCode);
        }

        String bearer = kisTokenService.getAccessToken();

        String url = kisProperties.getBaseUrl()
                + "/uapi/domestic-stock/v1/quotations/search-stock-info"
                + "?PRDT_TYPE_CD=300"
                + "&PDNO=" + stockCode;

        KisSearchStockInfoResponse response = kisApiRequestCoordinator.execute(
                () -> restClient.get()
                        .uri(url)
                        .headers(headers -> addKisHeaders(headers, "CTPF1002R", bearer))
                        .retrieve()
                        .body(KisSearchStockInfoResponse.class));

        if (response == null) {
            throw new IllegalStateException("한국투자증권 종목 요약 응답이 없습니다.");
        }

        if (!"0".equals(response.rt_cd())) {
            throw new IllegalStateException("한국투자증권 종목 요약 조회 실패: " + response.msg1());
        }

        Map<String, Object> output = safeMap(response.output());

        return new KisStockSummaryResponse(
                stockCode,
                valueToString(output.get("prdt_name")),
                valueToString(output.get("mket_id_cd")),
                valueToString(output.get("scty_grp_id_cd")),
                valueToString(output.get("stck_kind_cd")),
                valueToString(output.get("lstg_stqt")),
                valueToString(output.get("cpta")),
                valueToString(output.get("papr")),
                firstNonBlank(output, "scts_mket_lstg_dt", "kosdaq_mket_lstg_dt", "frbd_mket_lstg_dt"),
                valueToString(output.get("setl_mmdd")),
                valueToString(output.get("kospi200_item_yn")),
                valueToString(output.get("std_pdno")),
                firstNonBlank(output, "scts_mket_lstg_abol_dt", "kosdaq_mket_lstg_abol_dt", "frbd_mket_lstg_abol_dt",
                        "lstg_abol_dt"));
    }

    public KisStockDetailResponse getStockDetail(String stockCode) {
        return new KisStockDetailResponse(
                getStockPrice(stockCode),
                stockStaticProfileService.getStaticProfile(stockCode));
    }

    private KisStockSummaryResponse buildLocalStockSummary(String stockCode) {
        StockInfoDto stockInfo = stockDao.getStockInfo(stockCode);

        if (stockInfo == null) {
            return new KisStockSummaryResponse(
                    stockCode,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);
        }

        return new KisStockSummaryResponse(
                stockCode,
                stockInfo.getStockName(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
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

    private List<KisStockOrderbookResponse.OrderbookLevel> buildOrderbookLevels(
            Map<String, Object> output,
            String pricePrefix,
            String quantityPrefix,
            String changePrefix) {
        List<KisStockOrderbookResponse.OrderbookLevel> levels = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            levels.add(
                    new KisStockOrderbookResponse.OrderbookLevel(
                            i,
                            valueToString(output.get(pricePrefix + i)),
                            valueToString(output.get(quantityPrefix + i)),
                            valueToString(output.get(changePrefix + i))));
        }

        return levels;
    }

    private Map<String, Object> safeMap(Map<String, Object> output) {
        return output == null ? Collections.emptyMap() : output;
    }

    private String firstNonBlank(
            Map<String, Object> output,
            String... keys) {
        for (String key : keys) {
            String value = valueToString(output.get(key));

            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return null;
    }

    private String valueToString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private <T> T getCached(
            ConcurrentHashMap<String, CachedValue<T>> cache,
            String stockCode,
            long ttlMillis) {
        String key = cacheKey(stockCode);
        CachedValue<T> cached = cache.get(key);

        if (cached == null) {
            return null;
        }

        if (System.currentTimeMillis() - cached.createdAtMillis() > ttlMillis) {
            cache.remove(key, cached);
            return null;
        }

        return cached.value();
    }

    private <T> void putCached(
            ConcurrentHashMap<String, CachedValue<T>> cache,
            String stockCode,
            T value) {
        if (value == null) {
            return;
        }

        cache.put(cacheKey(stockCode), new CachedValue<>(value, System.currentTimeMillis()));
    }

    private String cacheKey(String stockCode) {
        return stockCode == null ? "" : stockCode.trim();
    }

    private record CachedValue<T>(
            T value,
            long createdAtMillis) {
    }

    private record KisInquirePriceResponse(
            String rt_cd,
            String msg_cd,
            String msg1,
            Map<String, Object> output) {
    }

    private record KisOrderbookApiResponse(
            String rt_cd,
            String msg_cd,
            String msg1,
            Map<String, Object> output1,
            Map<String, Object> output2) {
    }

    private record KisSearchStockInfoResponse(
            String rt_cd,
            String msg_cd,
            String msg1,
            Map<String, Object> output) {
    }

}
