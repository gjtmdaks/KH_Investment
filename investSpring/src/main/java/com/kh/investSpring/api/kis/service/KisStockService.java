package com.kh.investSpring.api.kis.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.kh.investSpring.api.dart.service.StockStaticProfileService;
import com.kh.investSpring.api.kis.config.KisProperties;
import com.kh.investSpring.api.kis.dao.StockRealtimeDao;
import com.kh.investSpring.api.kis.dto.KisStockDetailResponse;
import com.kh.investSpring.api.kis.dto.StockOrderbookSource;
import com.kh.investSpring.api.kis.dto.StockOrderbookViewResponse;
import com.kh.investSpring.api.kis.dto.StockPriceSource;
import com.kh.investSpring.api.kis.dto.StockPriceViewResponse;
import com.kh.investSpring.api.kis.dto.StockRealtimeCurrentDto;
import com.kh.investSpring.api.kis.dto.KisStockOrderbookResponse;
import com.kh.investSpring.api.kis.dto.KisStockPriceResponse;
import com.kh.investSpring.api.kis.dto.KisStockSummaryResponse;
import com.kh.investSpring.api.kis.http.KisInquireCcnlHttpResponse;
import com.kh.investSpring.api.kis.http.KisInquirePriceHttpResponse;
import com.kh.investSpring.api.kis.http.KisOrderbookHttpResponse;
import com.kh.investSpring.api.kis.http.KisSearchStockInfoHttpResponse;
import com.kh.investSpring.api.kis.market.KisMarketDivCode;
import com.kh.investSpring.api.kis.market.QuoteSession;
import com.kh.investSpring.domain.stock.dao.StockDao;
import com.kh.investSpring.domain.stock.dto.StockInfoDto;

@Service
public class KisStockService {

    private static final long ORDERBOOK_CACHE_TTL_MS = 1_000L;
    private static final long REFERENCE_CACHE_FALLBACK_TTL_MS = 300_000L;
    private static final String BATCH_CHG_CACHE_PREFIX = "invest:kis:batch:chg:";
    private static final Duration BATCH_CHG_TTL = Duration.ofSeconds(45);
    private final RestClient restClient;
    private final KisProperties kisProperties;
    private final KisTokenService kisTokenService;
    private final KisApiRequestCoordinator kisApiRequestCoordinator;
    private final StockDao stockDao;
    private final StockRealtimeDao stockRealtimeDao;
    private final StockStaticProfileService stockStaticProfileService;
    private final KisStockPriceSharedCache priceSharedCache;
    private final KisSubscriptionPoolService subscriptionPoolService;
    private final KisOrderbookRealtimeCache orderbookRealtimeCache;
    private final KisOrderbookDemandSubscriptionService orderbookDemandSubscriptionService;
    private final KisMarketQuoteSupport marketQuoteSupport;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private record ResolvedStockPrice(
            KisStockPriceResponse body,
            StockPriceSource source,
            QuoteSession quoteSession,
            KisMarketDivCode marketDivCode,
            boolean stale) {
    }

    private record FetchedStockPrice(KisStockPriceResponse body, KisMarketDivCode marketDivCode) {
    }

    private record FetchedOrderbook(KisStockOrderbookResponse body, KisMarketDivCode marketDivCode) {
    }
    private final ConcurrentHashMap<String, CachedValue<KisStockPriceResponse>> priceCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CachedValue<KisStockPriceResponse>> referencePriceCache =
            new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CachedValue<KisStockOrderbookResponse>> orderbookCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> priceInflightLocks = new ConcurrentHashMap<>();

    public KisStockService(
            RestClient restClient,
            KisProperties kisProperties,
            KisTokenService kisTokenService,
            KisApiRequestCoordinator kisApiRequestCoordinator,
            StockDao stockDao,
            StockRealtimeDao stockRealtimeDao,
            StockStaticProfileService stockStaticProfileService,
            KisStockPriceSharedCache priceSharedCache,
            KisSubscriptionPoolService subscriptionPoolService,
            KisOrderbookRealtimeCache orderbookRealtimeCache,
            KisOrderbookDemandSubscriptionService orderbookDemandSubscriptionService,
            KisMarketQuoteSupport marketQuoteSupport,
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.kisProperties = kisProperties;
        this.kisTokenService = kisTokenService;
        this.kisApiRequestCoordinator = kisApiRequestCoordinator;
        this.stockDao = stockDao;
        this.stockRealtimeDao = stockRealtimeDao;
        this.stockStaticProfileService = stockStaticProfileService;
        this.priceSharedCache = priceSharedCache;
        this.subscriptionPoolService = subscriptionPoolService;
        this.orderbookRealtimeCache = orderbookRealtimeCache;
        this.orderbookDemandSubscriptionService = orderbookDemandSubscriptionService;
        this.marketQuoteSupport = marketQuoteSupport;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public boolean subscribeOrderbookDemand(String stockCode) {
        return orderbookDemandSubscriptionService.subscribeDemand(stockCode);
    }

    public void unsubscribeOrderbookDemand(String stockCode) {
        orderbookDemandSubscriptionService.unsubscribeDemand(stockCode);
    }

    /**
     * 구독 종목의 당일 누적 거래량·거래대금을 KIS REST(acml_vol, acml_tr_pbmn)로 DB에 반영한다.
     * WS tick의 체결량(CNTG_VOL)과 혼동되지 않도록 REST 전용 경로이다.
     */
    public void backfillVolumeAndTradingValue(String stockCode) {
        String code = cacheKey(stockCode);
        if (code.isBlank()) {
            return;
        }

        StockRealtimeCurrentDto row = stockRealtimeDao.findRealtimeCurrentByStockCode(code);
        if (row == null) {
            return;
        }

        try {
            QuoteSession session = marketQuoteSupport.currentSession();
            FetchedStockPrice fetched = fetchStockPriceFromKis(code, session);
            KisStockPriceResponse fromKis = fetched.body();
            String scopedKey = scopedCacheKey(code, session, fetched.marketDivCode());
            storeReferencePriceCache(scopedKey, fromKis);

            Long volume = parseLongFromString(fromKis.volume());
            Long tradingValue = parseLongFromString(fromKis.tradingValue());
            if (tradingValue == null
                    && volume != null
                    && row.getCurrentPrice() != null) {
                tradingValue = row.getCurrentPrice() * volume;
            }

            stockRealtimeDao.updateVolumeAndTradingValue(
                    code,
                    volume,
                    tradingValue,
                    LocalDateTime.now());
        } catch (Exception ignored) {
            return;
        }
    }

    public KisStockPriceResponse getStockPrice(String stockCode) {
        return resolveStockPrice(stockCode).body();
    }

    public StockPriceViewResponse getStockPriceView(String stockCode) {
        String code = cacheKey(stockCode);
        if (code.isBlank()) {
            throw new IllegalArgumentException("종목코드가 비어 있습니다.");
        }

        ResolvedStockPrice resolved = resolveStockPrice(stockCode);
        
        return StockPriceViewResponse.from(
                resolved.body(),
                isWsSubscribed(code),
                resolved.source(),
                resolved.quoteSession().name(),
                resolved.marketDivCode().code(),
                marketQuoteSupport.asOfNow(),
                resolved.stale());
    }

    private ResolvedStockPrice resolveStockPrice(String stockCode) {
        String code = cacheKey(stockCode);
        if (code.isBlank()) {
            throw new IllegalArgumentException("종목코드가 비어 있습니다.");
        }

        QuoteSession session = marketQuoteSupport.currentSession();
        KisMarketDivCode marketDivCode = marketQuoteSupport.marketDivCode(session);
        String scopedKey = scopedCacheKey(code, session, marketDivCode);

        KisStockPriceResponse local = getCached(
                priceCache,
                scopedKey,
                kisProperties.getPriceLocalCacheTtlMs());
        if (local != null) {
            return new ResolvedStockPrice(local, StockPriceSource.LOCAL, session, marketDivCode, marketQuoteSupport.isStale(session));
        }

        Optional<KisStockPriceResponse> redis = priceSharedCache.get(scopedKey);
        if (redis.isPresent()) {
            return new ResolvedStockPrice(redis.get(), StockPriceSource.REDIS, session, marketDivCode, marketQuoteSupport.isStale(session));
        }

        Object lock = priceInflightLocks.computeIfAbsent(scopedKey, ignored -> new Object());
        synchronized (lock) {
            try {
                local = getCached(
                        priceCache,
                        scopedKey,
                        kisProperties.getPriceLocalCacheTtlMs());
                if (local != null) {
                    return new ResolvedStockPrice(local, StockPriceSource.LOCAL, session, marketDivCode, marketQuoteSupport.isStale(session));
                }

                Optional<KisStockPriceResponse> redisAfterLock = priceSharedCache.get(scopedKey);
                if (redisAfterLock.isPresent()) {
                    return new ResolvedStockPrice(redisAfterLock.get(), StockPriceSource.REDIS, session, marketDivCode, marketQuoteSupport.isStale(session));
                }

                KisStockPriceResponse fromDb = resolvePriceFromDbIfFresh(code, scopedKey);
                if (fromDb != null) {
                    storePriceCaches(scopedKey, fromDb);
                    return new ResolvedStockPrice(fromDb, StockPriceSource.DB, session, marketDivCode, marketQuoteSupport.isStale(session));
                }

                FetchedStockPrice fetched = fetchStockPriceFromKis(code, session);
                KisStockPriceResponse fromKis = fetched.body();
                String fetchedKey = scopedCacheKey(code, session, fetched.marketDivCode());
                storeReferencePriceCache(fetchedKey, fromKis);
                storePriceCaches(fetchedKey, fromKis);
                return new ResolvedStockPrice(fromKis, StockPriceSource.KIS, session, fetched.marketDivCode(), marketQuoteSupport.isStale(session));
            } finally {
                priceInflightLocks.remove(scopedKey, lock);
            }
        }
    }

    private boolean isWsSubscribed(String stockCode) {
        if (!kisProperties.isWebsocketEnabled()) {
            return false;
        }
        return subscriptionPoolService.getSubscribedCodes().contains(stockCode);
    }

    private void storePriceCaches(String stockCode, KisStockPriceResponse response) {
        putCached(priceCache, stockCode, response);
        priceSharedCache.put(stockCode, response);
    }

    private KisStockPriceResponse resolvePriceFromDbIfFresh(String stockCode, String scopedKey) {
        StockRealtimeCurrentDto row = stockRealtimeDao.findRealtimeCurrentByStockCode(stockCode);
        if (row == null || row.getCurrentPrice() == null || row.getUpdatedAt() == null) {
            return null;
        }

        long ageMs = Duration.between(row.getUpdatedAt(), LocalDateTime.now()).toMillis();
        if (ageMs < 0L) {
            ageMs = 0L;
        }
        if (ageMs > kisProperties.getPriceDbFreshTtlMs()) {
            return null;
        }

        KisStockPriceResponse realtime = buildPriceFromRealtimeCurrent(row);
        KisStockPriceResponse reference = resolveReferencePrice(stockCode, scopedKey);
        return mergeRealtimeWithReference(realtime, reference);
    }

    private KisStockPriceResponse buildPriceFromRealtimeCurrent(StockRealtimeCurrentDto row) {
        String currentPrice = formatNumber(row.getCurrentPrice());
        String volume = formatNumber(row.getVolume());
        String tradingValue = formatNumber(row.getTradingValue());
        if (tradingValue == null
                && row.getCurrentPrice() != null
                && row.getVolume() != null) {
            tradingValue = String.valueOf(row.getCurrentPrice() * row.getVolume());
        }

        return new KisStockPriceResponse(
                row.getStockCode(),
                row.getStockName(),
                currentPrice,
                formatNumber(row.getChangePrice()),
                formatChangeRate(row.getChangeRate()),
                volume,
                tradingValue,
                formatNumber(row.getOpenPrice()),
                null,
                null,
                null);
    }

    private KisStockPriceResponse resolveReferencePrice(String stockCode, String scopedKey) {
        KisStockPriceResponse cached = getCached(
                referencePriceCache,
                scopedKey,
                kisProperties.getPriceReferenceCacheTtlMs());
        if (cached != null) {
            return cached;
        }

        KisStockPriceResponse fallback = getCached(
                priceCache,
                scopedKey,
                REFERENCE_CACHE_FALLBACK_TTL_MS);
        if (fallback != null && hasReferenceFields(fallback)) {
            return fallback;
        }

        try {
            FetchedStockPrice fetched = fetchStockPriceFromKis(stockCode, marketQuoteSupport.currentSession());
            KisStockPriceResponse fromKis = fetched.body();
            storeReferencePriceCache(scopedKey, fromKis);
            return fromKis;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private void storeReferencePriceCache(String stockCode, KisStockPriceResponse response) {
        putCached(referencePriceCache, stockCode, response);
    }

    private static boolean hasReferenceFields(KisStockPriceResponse response) {
        return response != null
                && (response.highPrice() != null
                        || response.lowPrice() != null
                        || response.executionStrength() != null);
    }

    private static KisStockPriceResponse mergeRealtimeWithReference(
            KisStockPriceResponse realtime,
            KisStockPriceResponse reference) {
        if (reference == null) {
            return realtime;
        }

        return new KisStockPriceResponse(
                realtime.stockCode(),
                coalesceNonBlank(realtime.stockName(), reference.stockName()),
                realtime.currentPrice(),
                coalesceNonBlank(realtime.changePrice(), reference.changePrice()),
                coalesceNonBlank(realtime.changeRate(), reference.changeRate()),
                coalesceNonBlank(reference.volume(), realtime.volume()),
                coalesceNonBlank(reference.tradingValue(), realtime.tradingValue()),
                coalesceNonBlank(realtime.openPrice(), reference.openPrice()),
                reference.highPrice(),
                reference.lowPrice(),
                reference.executionStrength());
    }

    private static String coalesceNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback;
    }

    private FetchedStockPrice fetchStockPriceFromKis(String stockCode, QuoteSession session) {
        KisMarketDivCode primary = marketQuoteSupport.marketDivCode(session);
        try {
            return new FetchedStockPrice(fetchStockPriceFromKis(stockCode, primary), primary);
        } catch (Exception e) {
            if (primary == KisMarketDivCode.KRX) {
                throw e;
            }

            return new FetchedStockPrice(fetchStockPriceFromKis(stockCode, KisMarketDivCode.KRX), KisMarketDivCode.KRX);
        }
    }

    private KisStockPriceResponse fetchStockPriceFromKis(String stockCode) {
        return fetchStockPriceFromKis(stockCode, marketQuoteSupport.currentSession()).body();
    }

    private KisStockPriceResponse fetchStockPriceFromKis(String stockCode, KisMarketDivCode marketDivCode) {
        String accessToken = kisTokenService.getAccessToken();

        String url = kisProperties.getBaseUrl()
                + "/uapi/domestic-stock/v1/quotations/inquire-price"
                + "?FID_COND_MRKT_DIV_CODE=" + marketDivCode.code()
                + "&FID_INPUT_ISCD=" + stockCode;

        KisInquirePriceHttpResponse response = kisApiRequestCoordinator.execute(
                () -> restClient.get()
                        .uri(url)
                        .header("content-type", "application/json; charset=utf-8")
                        .header("authorization", "Bearer " + accessToken)
                        .header("appkey", kisProperties.getAppKey())
                        .header("appsecret", kisProperties.getAppSecret())
                        .header("tr_id", "FHKST01010100")
                        .retrieve()
                        .body(KisInquirePriceHttpResponse.class));

        if (response == null) {
            throw new IllegalStateException("한국투자증권 현재가 응답이 없습니다.");
        }

        if (!"0".equals(response.rt_cd())) {
            throw new IllegalStateException("한국투자증권 현재가 조회 실패: " + response.msg1());
        }

        Map<String, Object> output = safeMap(response.output());
        String executionStrength = fetchExecutionStrength(stockCode, accessToken, marketDivCode);

        return new KisStockPriceResponse(
                stockCode,
                valueToString(output.get("hts_kor_isnm")),
                valueToString(output.get("stck_prpr")),
                valueToString(output.get("prdy_vrss")),
                valueToString(output.get("prdy_ctrt")),
                valueToString(output.get("acml_vol")),
                valueToString(output.get("acml_tr_pbmn")),
                valueToString(output.get("stck_oprc")),
                valueToString(output.get("stck_hgpr")),
                valueToString(output.get("stck_lwpr")),
                executionStrength);
    }

    private static Long parseLongFromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String formatNumber(Long value) {
        return value == null ? null : String.valueOf(value);
    }

    private static String formatChangeRate(Double value) {
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    /**
     * 당일 체결강도(tday_rltv)는 inquire-price가 아닌 inquire-ccnl(FHKST01010300) 응답에 포함된다.
     */
    private String fetchExecutionStrength(String stockCode, String accessToken, KisMarketDivCode marketDivCode) {
        try {
            String url = kisProperties.getBaseUrl()
                    + "/uapi/domestic-stock/v1/quotations/inquire-ccnl"
                    + "?FID_COND_MRKT_DIV_CODE=" + marketDivCode.code()
                    + "&FID_INPUT_ISCD=" + stockCode;

            KisInquireCcnlHttpResponse ccnlResponse = kisApiRequestCoordinator.execute(
                    () -> restClient.get()
                            .uri(url)
                            .headers(headers -> addKisHeaders(headers, "FHKST01010300", accessToken))
                            .retrieve()
                            .body(KisInquireCcnlHttpResponse.class));

            if (ccnlResponse == null || !"0".equals(ccnlResponse.rt_cd())) {
                return null;
            }

            List<Map<String, Object>> rows = ccnlResponse.output();
            if (rows == null || rows.isEmpty()) {
                return null;
            }

            for (Map<String, Object> row : rows) {
                if (row == null) {
                    continue;
                }

                String strength = firstNonBlank(row, "tday_rltv", "cttr");
                if (strength != null && !strength.isBlank()) {
                    return strength;
                }
            }

            return null;
        } catch (Exception ignored) {
            return null;
        }
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
        QuoteSession session = marketQuoteSupport.currentSession();
        KisMarketDivCode marketDivCode = marketQuoteSupport.marketDivCode(session);
        String cacheKey =
                BATCH_CHG_CACHE_PREFIX
                        + marketQuoteSupport.cacheSuffix(session, marketDivCode)
                        + ":"
                        + fingerprintSortedCodes(sorted);
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

    public StockOrderbookViewResponse getStockOrderbookView(String stockCode) {
        String code = cacheKey(stockCode);
        if (code.isBlank()) {
            throw new IllegalArgumentException("종목코드가 비어 있습니다.");
        }

        QuoteSession session = marketQuoteSupport.currentSession();
        KisMarketDivCode marketDivCode = marketQuoteSupport.marketDivCode(session);
        boolean wsSubscribed = orderbookDemandSubscriptionService.isDemandSubscribed(code);

        Optional<KisStockOrderbookResponse> fromWs = orderbookRealtimeCache.getFresh(
                code,
                kisProperties.getOrderbookWsFreshTtlMs());
        if (fromWs.isPresent()) {
            return StockOrderbookViewResponse.from(
                    fromWs.get(),
                    wsSubscribed,
                    StockOrderbookSource.WS,
                    session.name(),
                    marketDivCode.code(),
                    marketQuoteSupport.asOfNow(),
                    marketQuoteSupport.isStale(session));
        }

        FetchedOrderbook fromRest = fetchStockOrderbookFromRest(stockCode, session, marketDivCode);
        return StockOrderbookViewResponse.from(
                fromRest.body(),
                wsSubscribed,
                StockOrderbookSource.REST,
                session.name(),
                fromRest.marketDivCode().code(),
                marketQuoteSupport.asOfNow(),
                marketQuoteSupport.isStale(session));
    }

    private FetchedOrderbook fetchStockOrderbookFromRest(
            String stockCode,
            QuoteSession session,
            KisMarketDivCode marketDivCode) {
        String scopedKey = scopedCacheKey(stockCode, session, marketDivCode);
        KisStockOrderbookResponse cached = getCached(orderbookCache, scopedKey, ORDERBOOK_CACHE_TTL_MS);
        if (cached != null) {
            return new FetchedOrderbook(cached, marketDivCode);
        }

        try {
            KisStockOrderbookResponse result = fetchStockOrderbookFromRest(stockCode, marketDivCode);
            putCached(orderbookCache, scopedKey, result);
            return new FetchedOrderbook(result, marketDivCode);
        } catch (Exception e) {
            if (marketDivCode == KisMarketDivCode.KRX) {
                throw e;
            }

            String fallbackKey = scopedCacheKey(stockCode, session, KisMarketDivCode.KRX);
            KisStockOrderbookResponse fallbackCached = getCached(orderbookCache, fallbackKey, ORDERBOOK_CACHE_TTL_MS);
            if (fallbackCached != null) {
                return new FetchedOrderbook(fallbackCached, KisMarketDivCode.KRX);
            }

            KisStockOrderbookResponse fallback = fetchStockOrderbookFromRest(stockCode, KisMarketDivCode.KRX);
            putCached(orderbookCache, fallbackKey, fallback);
            return new FetchedOrderbook(fallback, KisMarketDivCode.KRX);
        }
    }

    private KisStockOrderbookResponse fetchStockOrderbookFromRest(
            String stockCode,
            KisMarketDivCode marketDivCode) {
        String bearer = kisTokenService.getAccessToken();

        String url = kisProperties.getBaseUrl()
                + "/uapi/domestic-stock/v1/quotations/inquire-asking-price-exp-ccn"
                + "?FID_COND_MRKT_DIV_CODE=" + marketDivCode.code()
                + "&FID_INPUT_ISCD=" + stockCode;

        KisOrderbookHttpResponse response = kisApiRequestCoordinator.execute(
                () -> restClient.get()
                        .uri(url)
                        .headers(headers -> addKisHeaders(headers, "FHKST01010200", bearer))
                        .retrieve()
                        .body(KisOrderbookHttpResponse.class));

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

        KisSearchStockInfoHttpResponse response = kisApiRequestCoordinator.execute(
                () -> restClient.get()
                        .uri(url)
                        .headers(headers -> addKisHeaders(headers, "CTPF1002R", bearer))
                        .retrieve()
                        .body(KisSearchStockInfoHttpResponse.class));

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
                getStockPriceView(stockCode),
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

    private String scopedCacheKey(
            String stockCode,
            QuoteSession session,
            KisMarketDivCode marketDivCode) {
        String code = cacheKey(stockCode);
        if (code.isBlank()) {
            return "";
        }

        return code + ":" + marketQuoteSupport.cacheSuffix(session, marketDivCode);
    }

    private record CachedValue<T>(T value, long createdAtMillis) {
    }
    
    public KisStockPriceResponse fetchPriceFromKisDirect(String stockCode) {
        String code = cacheKey(stockCode);

        if (code.isBlank()) {
            throw new IllegalArgumentException("종목코드가 비어 있습니다.");
        }

        KisStockPriceResponse response = fetchStockPriceFromKis(code);
        storeReferencePriceCache(code, response);
        storePriceCaches(code, response);

        return response;
    }

}
