package com.kh.investSpring.domain.news.service;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.investSpring.api.kis.service.KisStockService;
import com.kh.investSpring.api.naver.NaverNewsApiClient;
import com.kh.investSpring.api.naver.dto.NaverNewsItemDto;
import com.kh.investSpring.domain.news.dao.NewsDao;
import com.kh.investSpring.domain.news.dto.NewsInfoEntity;
import com.kh.investSpring.domain.news.dto.NewsRelatedStockRow;
import com.kh.investSpring.domain.news.dto.NewsResponse;
import com.kh.investSpring.domain.news.dto.RelatedStock;
import com.kh.investSpring.domain.news.service.NewsKeywordLabelService.RelatedStockMatch;
import com.kh.investSpring.domain.news.util.FinanceNewsTopicFilter;
import com.kh.investSpring.domain.news.util.NewsContentMergeUtil;
import com.kh.investSpring.domain.stock.dao.StockDao;
import com.kh.investSpring.domain.stock.dto.StockInfoDto;
import com.kh.investSpring.global.util.HtmlStripUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsServiceImpl implements NewsService {

	private static final String CACHE_MARKET = "invest:news:market:v5";
	private static final String CACHE_STOCK_PREFIX = "invest:news:stock:v5:";
	private static final String CACHE_MARKET_OLD = "invest:news:market:v4";
	private static final String CACHE_STOCK_PREFIX_OLD = "invest:news:stock:v4:";

	/** 뉴스 1건당 관련 종목 칩 최대 노출 개수 */
	private static final int RELATED_STOCKS_MAX = 5;
	private static final int STOCK_NEWS_FETCH_MULTIPLIER = 2;
	private static final int STOCK_NEWS_MIN_FETCH_SIZE = 12;
	/** 공개 시장 뉴스 목록·Redis 캐시 최대 건수 */
	private static final int MARKET_NEWS_MAX_DISPLAY = 100;
	private static final DateTimeFormatter NAVER_PUB = DateTimeFormatter.ofPattern(
			"EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
	private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

	private final NewsDao newsDao;
	private final StockDao stockDao;
	private final StringRedisTemplate redis;
	private final ObjectMapper objectMapper;
	private final NaverNewsApiClient naverNewsApiClient;
	private final NewsKeywordLabelService newsKeywordLabelService;
	private final NewsOgEnrichmentService newsOgEnrichmentService;
	private final KisStockService kisStockService;

	/** 시장 뉴스 캐시 미스 시 중복 ingest(워밍·첫 요청 동시) 방지 */
	private final Object marketNewsFetchLock = new Object();

	@Override
	public List<NewsResponse> getMarketNews(int size) {
		int n = clampDisplay(size);
		invalidateOldCacheOnce(CACHE_MARKET_OLD);
		List<NewsResponse> cached = readListFromRedis(CACHE_MARKET, n);
		if (!cached.isEmpty()) {
			return cached;
		}

		synchronized (marketNewsFetchLock) {
			cached = readListFromRedis(CACHE_MARKET, n);
			if (!cached.isEmpty()) {
				return cached;
			}

			int fetchSize = Math.min(100, Math.max(n * 4, 40));
			List<NaverNewsItemDto> items = naverNewsApiClient.searchNews(FinanceNewsTopicFilter.MARKET_SEARCH_QUERY, fetchSize)
					.stream()
					.filter(FinanceNewsTopicFilter::passesNaverItem)
					.toList();
			if (!items.isEmpty()) {
				List<NewsResponse> fresh = ingestMarketItems(items, n);
				cacheList(CACHE_MARKET, fresh, Duration.ofMinutes(5));
				return fresh;
			}

			List<NewsResponse> fallback = mapEntities(newsDao.selectRecentNewsInfo(n));
			cacheList(CACHE_MARKET, fallback, Duration.ofMinutes(5));
			return fallback;
		}
	}

	@Override
	public List<NewsResponse> getMarketNewsByTag(String tag, int size) {
		int n = clampDisplay(size);
		String t = tag == null ? "" : tag.trim();
		if (t.isEmpty()) {
			return getMarketNews(n);
		}
		// 캐시된 전체 리스트를 재사용하고 서버에서 필터링(캐시 키 폭발 방지)
		List<NewsResponse> base = getMarketNews(Math.min(50, Math.max(n * 4, 40)));
		List<NewsResponse> out = new ArrayList<>();
		for (NewsResponse r : base) {
			if (r == null) continue;
			if (r.primaryLabel() != null && r.primaryLabel().trim().equalsIgnoreCase(t)) {
				out.add(r);
			}
			if (out.size() >= n) {
				break;
			}
		}
		return out;
	}

	@Override
	public List<NewsResponse> getStockNews(String stockCode, int size) {
		int n = clampDisplay(size);
		String code = stockCode == null ? "" : stockCode.trim();
		if (code.isEmpty()) {
			return List.of();
		}
		String cacheKeyOld = CACHE_STOCK_PREFIX_OLD + code;
		invalidateOldCacheOnce(cacheKeyOld);
		String cacheKey = CACHE_STOCK_PREFIX + code;
		List<NewsResponse> cached = readListFromRedis(cacheKey, n);
		if (!cached.isEmpty()) {
			return cached;
		}

		List<NewsResponse> fallback = mapEntities(newsDao.selectNewsInfoByStockCode(code, n));
        if (!fallback.isEmpty()) {
            cacheList(cacheKey, fallback, Duration.ofMinutes(3));
            return fallback;
        }


		String queryKeyword = resolveStockSearchKeyword(code);
		int fetchSize = Math.min(100, Math.max(n * STOCK_NEWS_FETCH_MULTIPLIER, STOCK_NEWS_MIN_FETCH_SIZE));
		List<NaverNewsItemDto> items = naverNewsApiClient.searchNews(queryKeyword.trim(), fetchSize)
				.stream()
				.filter(FinanceNewsTopicFilter::passesNaverItem)
				.toList();
		if (!items.isEmpty()) {
			List<NewsResponse> fresh = ingestStockItems(items, code, n);
			cacheList(cacheKey, fresh, Duration.ofMinutes(3));
			return fresh;
		}

		cacheList(cacheKey, fallback, Duration.ofMinutes(3));
		return fallback;
	}

	private List<NewsResponse> ingestMarketItems(List<NaverNewsItemDto> items, int limit) {
		List<NewsResponse> out = new ArrayList<>();
		for (NaverNewsItemDto item : items) {
			NewsResponse row = persistOne(item, null);
			if (row != null) {
				out.add(row);
			}
			if (out.size() >= limit) {
				break;
			}
		}
		return overlayKisRatesForExistingStocks(enrichRelatedStockRates(out));
	}

	private List<NewsResponse> ingestStockItems(List<NaverNewsItemDto> items, String stockCode, int limit) {
		List<NewsResponse> out = new ArrayList<>();
		for (NaverNewsItemDto item : items) {
			NewsResponse row = persistOne(item, stockCode);
			if (row != null) {
				out.add(row);
			}
			if (out.size() >= limit) {
				break;
			}
		}
		return overlayKisRatesForExistingStocks(enrichRelatedStockRates(out));
	}

	private NewsResponse persistOne(NaverNewsItemDto item, String stockCodeOrNull) {
		String articleLink = normalizeArticleLink(item);
		if (articleLink.isEmpty()) {
			return null;
		}
		String naverTitle = NewsContentMergeUtil.truncate(HtmlStripUtil.stripHtml(item.title()), 500);
		String naverDescription = NewsContentMergeUtil.truncate(HtmlStripUtil.stripHtml(item.description()), 4000);
		String title = naverTitle;
		String description = naverDescription;
		String publisher = publisherFromUrl(articleLink);
		Date publishedAt = parseNaverPubDate(item.pubDate());

		NewsInfoEntity entity = new NewsInfoEntity();
		entity.setArticleLink(NewsContentMergeUtil.truncate(articleLink, 2000));
		entity.setNewsTitle(title);
		entity.setNewsDescription(description);
		entity.setPublisher(publisher);
		String extraStockKeyword = null;
		if (stockCodeOrNull != null && !stockCodeOrNull.isBlank()) {
			// 종목 뉴스의 경우 종목명(가능하면)을 후보로 추가하여 정확도 향상
			extraStockKeyword = resolveStockSearchKeyword(stockCodeOrNull.trim());
		}
		NewsKeywordLabelService.PrimaryKeyword picked =
				newsKeywordLabelService.detectPrimaryKeyword(title, description, extraStockKeyword);
		if (picked != null) {
			entity.setPrimaryLabel(picked.primaryLabel());
			entity.setKeywordKind(picked.keywordKind());
		}
		entity.setPublishedAt(publishedAt);

		// 다중 종목 매칭(최대 5개): mergeNewsInfoStock 일괄 저장 + 응답에 알고리즘 순서 보존
		List<RelatedStockMatch> matches =
				newsKeywordLabelService.detectRelatedStocks(title, description, RELATED_STOCKS_MAX);
		List<RelatedStock> algorithmOrderedStocks = toAlgorithmOrderedRelatedStocks(matches);
		if (log.isDebugEnabled()) {
			log.debug("관련 종목 매칭 size={} title={}", algorithmOrderedStocks.size(), title);
		}

		try {
			newsDao.upsertNewsInfo(entity);
		} catch (Exception e) {
			log.warn("NEWS_INFO MERGE 실패 link={}: {}", articleLink, e.getMessage());
			return buildResponseWithoutId(title, description, publisher, entity.getPrimaryLabel(), entity.getKeywordKind(), entity.getArticleLink(), publishedAt, algorithmOrderedStocks);
		}

		Long id = newsDao.selectNewsInfoIdByLink(entity.getArticleLink());
		if (id == null) {
			return buildResponseWithoutId(title, description, publisher, entity.getPrimaryLabel(), entity.getKeywordKind(), entity.getArticleLink(), publishedAt, algorithmOrderedStocks);
		}

		if (stockCodeOrNull != null && !stockCodeOrNull.isBlank()) {
			try {
				newsDao.mergeNewsInfoStock(id, stockCodeOrNull.trim());
			} catch (Exception e) {
				log.warn("NEWS_INFO_STOCK MERGE 실패 newsInfoId={} stock={}", id, stockCodeOrNull, e);
			}
		}

		// 알고리즘이 매칭한 관련 종목들도 NEWS_INFO_STOCK에 모두 적재(중복은 PK 제약으로 자동 무시)
		for (RelatedStock rs : algorithmOrderedStocks) {
			if (rs.stockCode() == null || rs.stockCode().isBlank()) continue;
			try {
				newsDao.mergeNewsInfoStock(id, rs.stockCode());
			} catch (Exception e) {
				log.debug("NEWS_INFO_STOCK MERGE 실패(관련 종목) newsInfoId={} stock={}: {}", id, rs.stockCode(), e.getMessage());
			}
		}

		newsOgEnrichmentService.enrichAfterPersist(id, naverTitle, naverDescription, entity.getArticleLink());

		return new NewsResponse(
				id,
				title,
				description,
				publisher,
				entity.getPrimaryLabel(),
				entity.getKeywordKind(),
				entity.getArticleLink(),
				toInstant(publishedAt),
				algorithmOrderedStocks);
	}

	private static NewsResponse buildResponseWithoutId(
			String title,
			String description,
			String publisher,
			String primaryLabel,
			String keywordKind,
			String articleLink,
			Date publishedAt,
			List<RelatedStock> relatedStocks) {
		return new NewsResponse(null, title, description, publisher, primaryLabel, keywordKind, articleLink, toInstant(publishedAt),
				relatedStocks == null ? List.of() : relatedStocks);
	}

	/**
	 * 알고리즘 매칭 결과를 응답 DTO 순서대로 변환합니다.
	 *
	 * <p>STOCKS 테이블 사전이 비어 있거나 부분 적재된 환경에서도 칩이 사라지지 않도록,
	 * {@code stockCode}가 null/blank인(정적 KEYWORDS 단독 매칭) 항목도 칩에는 노출합니다.
	 * 단, 이런 항목은 NEWS_INFO_STOCK 매핑·등락률 보강 대상에서 자연스럽게 제외됩니다.</p>
	 *
	 * <p>등락률은 1차로 null, 이후 {@link #enrichRelatedStockRates(List)}에서 채워집니다.</p>
	 */
	private static List<RelatedStock> toAlgorithmOrderedRelatedStocks(List<RelatedStockMatch> matches) {
		if (matches == null || matches.isEmpty()) {
			return List.of();
		}
		List<RelatedStock> out = new ArrayList<>(matches.size());
		for (RelatedStockMatch m : matches) {
			if (m == null) continue;
			if (m.stockName() == null || m.stockName().isBlank()) continue;
			String code = (m.stockCode() == null || m.stockCode().isBlank()) ? null : m.stockCode();
			out.add(new RelatedStock(code, m.stockName(), null));
		}
		return out;
	}

	private static Instant toInstant(Date date) {
		if (date == null) {
			return Instant.now();
		}
		return date.toInstant();
	}

	private List<NewsResponse> mapEntities(List<NewsInfoEntity> rows) {
		if (rows == null || rows.isEmpty()) {
			return List.of();
		}
		List<NewsResponse> list = new ArrayList<>(rows.size());
		for (NewsInfoEntity e : rows) {
			String title = HtmlStripUtil.stripHtml(e.getNewsTitle());
			String desc = HtmlStripUtil.stripHtml(e.getNewsDescription());
			if (!FinanceNewsTopicFilter.passesText(title, desc)) {
				continue;
			}
			list.add(new NewsResponse(
					e.getNewsInfoId(),
					title,
					desc,
					HtmlStripUtil.stripHtml(e.getPublisher()),
					e.getPrimaryLabel(),
					e.getKeywordKind(),
					e.getArticleLink(),
					toInstant(e.getPublishedAt()),
					List.of()));
		}
		// DB fallback 경로: NEWS_INFO_STOCK 매핑을 일괄 조회해 관련 종목을 채웁니다.
		return overlayKisRatesForExistingStocks(attachRelatedStocksFromDb(list));
	}

	/**
	 * ingest 직후 알고리즘 매칭으로 채워진 {@code relatedStocks}의 등락률(null)을 STOCK_REALTIME_TICK 최신값으로 보강합니다.
	 * <p>알고리즘 정렬 순서는 그대로 유지하며, 등락률만 매핑합니다.</p>
	 */
	private List<NewsResponse> enrichRelatedStockRates(List<NewsResponse> ingested) {
		if (ingested == null || ingested.isEmpty()) {
			return ingested == null ? List.of() : ingested;
		}
		List<Long> ids = new ArrayList<>();
		for (NewsResponse r : ingested) {
			if (r != null && r.newsInfoId() != null) {
				ids.add(r.newsInfoId());
			}
		}
		if (ids.isEmpty()) {
			return ingested;
		}
		Map<Long, Map<String, Double>> ratesByNewsId = loadChangeRateMap(ids);
		if (ratesByNewsId.isEmpty()) {
			return ingested;
		}
		List<NewsResponse> out = new ArrayList<>(ingested.size());
		for (NewsResponse r : ingested) {
			if (r == null || r.newsInfoId() == null || r.relatedStocks() == null || r.relatedStocks().isEmpty()) {
				out.add(r);
				continue;
			}
			Map<String, Double> rateMap = ratesByNewsId.getOrDefault(r.newsInfoId(), Map.of());
			List<RelatedStock> reordered = new ArrayList<>(r.relatedStocks().size());
			for (RelatedStock rs : r.relatedStocks()) {
				Double rate = (rs.stockCode() == null) ? null : rateMap.get(rs.stockCode());
				reordered.add(new RelatedStock(rs.stockCode(), rs.stockName(), rate));
			}
			out.add(new NewsResponse(
					r.newsInfoId(),
					r.title(),
					r.description(),
					r.publisher(),
					r.primaryLabel(),
					r.keywordKind(),
					r.articleLink(),
					r.publishedAt(),
					reordered));
		}
		return out;
	}

	/**
	 * {@link StockDao#getStockInfo(String)}로 등록된 종목만 골라 KIS 전일대비율을 채웁니다.
	 * Redis 캐시·워밍 직후 응답에 등락률이 포함되도록 ingest/fallback 경로의 마지막 단계에서 호출합니다.
	 */
	private List<NewsResponse> overlayKisRatesForExistingStocks(List<NewsResponse> rows) {
		if (rows == null || rows.isEmpty()) {
			return rows == null ? List.of() : rows;
		}
		LinkedHashSet<String> codes = new LinkedHashSet<>();
		for (NewsResponse r : rows) {
			if (r == null || r.relatedStocks() == null) {
				continue;
			}
			for (RelatedStock rs : r.relatedStocks()) {
				if (rs.stockCode() != null && !rs.stockCode().isBlank()) {
					codes.add(rs.stockCode().trim());
				}
			}
		}
		if (codes.isEmpty()) {
			return rows;
		}
		LinkedHashSet<String> existingCodes = new LinkedHashSet<>();
		for (String code : codes) {
			try {
				StockInfoDto info = stockDao.getStockInfo(code);
				if (info != null) {
					existingCodes.add(code);
				}
			} catch (Exception e) {
				log.debug("뉴스 관련종목 KIS 보강 시 종목 조회 생략 {}: {}", code, e.getMessage());
			}
		}
		if (existingCodes.isEmpty()) {
			return rows;
		}
		Map<String, String> kisRateStrings = new HashMap<>();
		List<String> codeList = new ArrayList<>(existingCodes);
		final int batch = 100;
		for (int i = 0; i < codeList.size(); i += batch) {
			int end = Math.min(i + batch, codeList.size());
			List<String> chunk = codeList.subList(i, end);
			try {
				kisRateStrings.putAll(kisStockService.getChangeRatesByStockCodes(new ArrayList<>(chunk)));
			} catch (Exception e) {
				log.warn("뉴스 관련종목 KIS 등락률 일괄 조회 실패: {}", e.getMessage());
			}
		}
		Map<String, Double> kisRates = new HashMap<>();
		for (Map.Entry<String, String> e : kisRateStrings.entrySet()) {
			Double d = parsePctToDouble(e.getValue());
			if (d != null) {
				kisRates.put(e.getKey(), d);
			}
		}
		if (kisRates.isEmpty()) {
			return rows;
		}
		List<NewsResponse> out = new ArrayList<>(rows.size());
		for (NewsResponse r : rows) {
			if (r == null || r.relatedStocks() == null || r.relatedStocks().isEmpty()) {
				out.add(r);
				continue;
			}
			List<RelatedStock> rebuilt = new ArrayList<>(r.relatedStocks().size());
			for (RelatedStock rs : r.relatedStocks()) {
				String c = rs.stockCode() == null ? null : rs.stockCode().trim();
				Double kis = (c == null) ? null : kisRates.get(c);
				Double use = kis != null ? kis : rs.changeRate();
				rebuilt.add(new RelatedStock(rs.stockCode(), rs.stockName(), use));
			}
			out.add(new NewsResponse(
					r.newsInfoId(),
					r.title(),
					r.description(),
					r.publisher(),
					r.primaryLabel(),
					r.keywordKind(),
					r.articleLink(),
					r.publishedAt(),
					rebuilt));
		}
		return out;
	}

	private static Double parsePctToDouble(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		try {
			return Double.parseDouble(raw.replace(",", "").trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * DB fallback 경로 전용: NEWS_INFO_STOCK에 적재된 매핑 그대로(종목코드 정렬)를 응답에 부착합니다.
	 * <p>매칭 순위 정보가 없으므로 STOCK_CODE 정렬을 기본 노출 순서로 사용합니다.</p>
	 */
	private List<NewsResponse> attachRelatedStocksFromDb(List<NewsResponse> rows) {
		if (rows == null || rows.isEmpty()) {
			return rows == null ? List.of() : rows;
		}
		List<Long> ids = new ArrayList<>();
		for (NewsResponse r : rows) {
			if (r != null && r.newsInfoId() != null) {
				ids.add(r.newsInfoId());
			}
		}
		if (ids.isEmpty()) {
			return rows;
		}
		Map<Long, List<RelatedStock>> grouped = groupRelatedStocks(ids);
		if (grouped.isEmpty()) {
			return rows;
		}
		List<NewsResponse> out = new ArrayList<>(rows.size());
		for (NewsResponse r : rows) {
			if (r == null || r.newsInfoId() == null) {
				out.add(r);
				continue;
			}
			List<RelatedStock> stocks = grouped.getOrDefault(r.newsInfoId(), List.of());
			if (stocks.size() > RELATED_STOCKS_MAX) {
				stocks = new ArrayList<>(stocks.subList(0, RELATED_STOCKS_MAX));
			}
			out.add(new NewsResponse(
					r.newsInfoId(),
					r.title(),
					r.description(),
					r.publisher(),
					r.primaryLabel(),
					r.keywordKind(),
					r.articleLink(),
					r.publishedAt(),
					stocks));
		}
		return out;
	}

	private Map<Long, Map<String, Double>> loadChangeRateMap(List<Long> ids) {
		List<NewsRelatedStockRow> rows = safeSelectRelatedStocks(ids);
		if (rows.isEmpty()) {
			return Map.of();
		}
		Map<Long, Map<String, Double>> out = new HashMap<>();
		for (NewsRelatedStockRow row : rows) {
			if (row == null || row.getNewsInfoId() == null || row.getStockCode() == null) continue;
			out.computeIfAbsent(row.getNewsInfoId(), k -> new HashMap<>())
					.put(row.getStockCode(), row.getChangeRate());
		}
		return out;
	}

	private Map<Long, List<RelatedStock>> groupRelatedStocks(List<Long> ids) {
		List<NewsRelatedStockRow> rows = safeSelectRelatedStocks(ids);
		if (rows.isEmpty()) {
			return Map.of();
		}
		Map<Long, List<RelatedStock>> out = new LinkedHashMap<>();
		for (NewsRelatedStockRow row : rows) {
			if (row == null || row.getNewsInfoId() == null || row.getStockCode() == null) continue;
			out.computeIfAbsent(row.getNewsInfoId(), k -> new ArrayList<>())
					.add(new RelatedStock(row.getStockCode(), row.getStockName(), row.getChangeRate()));
		}
		return out;
	}

	private List<NewsRelatedStockRow> safeSelectRelatedStocks(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return List.of();
		}
		try {
			List<NewsRelatedStockRow> rows = newsDao.selectRelatedStocksByNewsIds(ids);
			return rows == null ? List.of() : rows;
		} catch (Exception e) {
			log.warn("NEWS_INFO_STOCK 일괄 조회 실패 size={}: {}", ids.size(), e.getMessage());
			return List.of();
		}
	}

	private String resolveStockSearchKeyword(String stockCode) {
		try {
			StockInfoDto info = stockDao.getStockInfo(stockCode);
			if (info != null && info.getStockName() != null && !info.getStockName().isBlank()) {
				return info.getStockName().trim();
			}
		} catch (Exception e) {
			log.debug("종목명 조회 생략: {} — {}", stockCode, e.getMessage());
		}
		return stockCode;
	}

	private List<NewsResponse> readListFromRedis(String key, int limit) {
		try {
			Long len = redis.opsForList().size(key);
			if (len == null || len == 0) {
				return List.of();
			}
			List<String> raw = redis.opsForList().range(key, 0, Math.min(limit, len.intValue()) - 1);
			if (raw == null || raw.isEmpty()) {
				return List.of();
			}
			List<NewsResponse> out = new ArrayList<>(raw.size());
			for (String s : raw) {
				out.add(objectMapper.readValue(s, NewsResponse.class));
			}
			return out;
		} catch (Exception e) {
			log.debug("Redis 뉴스 캐시 읽기 실패 key={}: {}", key, e.getMessage());
			return List.of();
		}
	}

	private void invalidateOldCacheOnce(String oldKey) {
		if (oldKey == null || oldKey.isBlank()) {
			return;
		}
		try {
			Boolean existed = redis.hasKey(oldKey);
			if (Boolean.TRUE.equals(existed)) {
				redis.delete(oldKey);
			}
		} catch (Exception ignored) {
			// best-effort
		}
	}

	private void cacheList(String key, List<NewsResponse> data, Duration ttl) {
		if (data == null || data.isEmpty()) {
			return;
		}
		try {
			redis.delete(key);
			List<String> payloads = new ArrayList<>(data.size());
			for (NewsResponse r : data) {
				payloads.add(objectMapper.writeValueAsString(r));
			}
			redis.opsForList().rightPushAll(key, payloads);
			Boolean ok = redis.expire(key, ttl);
			if (Boolean.FALSE.equals(ok)) {
				log.debug("Redis TTL 설정 실패 key={}", key);
			}
		} catch (JsonProcessingException e) {
			log.warn("Redis 뉴스 직렬화 실패 key={}", key, e);
		} catch (Exception e) {
			log.warn("Redis 뉴스 캐시 저장 실패 key={}", key, e);
		}
	}

	private static int clampDisplay(int size) {
		if (size < 1) {
			return 1;
		}
		return Math.min(size, MARKET_NEWS_MAX_DISPLAY);
	}

	/** 네이버 기사 원문 URL 우선(originallink), 없으면 link */
	private static String normalizeArticleLink(NaverNewsItemDto item) {
		if (item.originallink() != null && !item.originallink().isBlank()) {
			return item.originallink().trim();
		}
		if (item.link() != null && !item.link().isBlank()) {
			return item.link().trim();
		}
		return "";
	}

	private static String publisherFromUrl(String url) {
		try {
			String host = URI.create(url).getHost();
			if (host != null && !host.isBlank()) {
				return NewsContentMergeUtil.truncate(host, 100);
			}
		} catch (Exception ignored) {
			// fall through
		}
		return "-";
	}

	private static Date parseNaverPubDate(String pubDate) {
		if (pubDate == null || pubDate.isBlank()) {
			return Date.from(Instant.now());
		}
		try {
			ZonedDateTime zdt = ZonedDateTime.parse(pubDate.trim(), NAVER_PUB);
			return Date.from(zdt.withZoneSameInstant(SEOUL).toInstant());
		} catch (Exception e) {
			return Date.from(Instant.now());
		}
	}
}
