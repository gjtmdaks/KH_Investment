package com.kh.investSpring.domain.news.service;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.investSpring.api.naver.NaverNewsApiClient;
import com.kh.investSpring.api.naver.dto.NaverNewsItemDto;
import com.kh.investSpring.domain.news.dao.NewsDao;
import com.kh.investSpring.domain.news.dto.NewsInfoEntity;
import com.kh.investSpring.domain.news.dto.NewsResponse;
import com.kh.investSpring.domain.news.util.FinanceNewsTopicFilter;
import com.kh.investSpring.domain.stock.dao.StockDao;
import com.kh.investSpring.domain.stock.dto.StockInfoDto;
import com.kh.investSpring.global.util.HtmlStripUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsServiceImpl implements NewsService {

	private static final String CACHE_MARKET = "invest:news:market:v2";
	private static final String CACHE_STOCK_PREFIX = "invest:news:stock:v2:";
	private static final DateTimeFormatter NAVER_PUB = DateTimeFormatter.ofPattern(
			"EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
	private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

	private final NewsDao newsDao;
	private final StockDao stockDao;
	private final StringRedisTemplate redis;
	private final ObjectMapper objectMapper;
	private final NaverNewsApiClient naverNewsApiClient;

	@Override
	public List<NewsResponse> getMarketNews(int size) {
		int n = clampDisplay(size);
		List<NewsResponse> cached = readListFromRedis(CACHE_MARKET, n);
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

	@Override
	public List<NewsResponse> getStockNews(String stockCode, int size) {
		int n = clampDisplay(size);
		String code = stockCode == null ? "" : stockCode.trim();
		if (code.isEmpty()) {
			return List.of();
		}
		String cacheKey = CACHE_STOCK_PREFIX + code;
		List<NewsResponse> cached = readListFromRedis(cacheKey, n);
		if (!cached.isEmpty()) {
			return cached;
		}

		String queryKeyword = resolveStockSearchKeyword(code);
		int fetchSize = Math.min(100, Math.max(n * 4, 40));
		List<NaverNewsItemDto> items = naverNewsApiClient.searchNews(queryKeyword.trim(), fetchSize)
				.stream()
				.filter(FinanceNewsTopicFilter::passesNaverItem)
				.toList();
		if (!items.isEmpty()) {
			List<NewsResponse> fresh = ingestStockItems(items, code, n);
			cacheList(cacheKey, fresh, Duration.ofMinutes(3));
			return fresh;
		}

		List<NewsResponse> fallback = mapEntities(newsDao.selectNewsInfoByStockCode(code, n));
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
		return out;
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
		return out;
	}

	private NewsResponse persistOne(NaverNewsItemDto item, String stockCodeOrNull) {
		String articleLink = normalizeArticleLink(item);
		if (articleLink.isEmpty()) {
			return null;
		}
		String title = truncate(HtmlStripUtil.stripHtml(item.title()), 500);
		String description = truncate(HtmlStripUtil.stripHtml(item.description()), 4000);
		String publisher = publisherFromUrl(articleLink);
		Date publishedAt = parseNaverPubDate(item.pubDate());

		NewsInfoEntity entity = new NewsInfoEntity();
		entity.setArticleLink(truncate(articleLink, 2000));
		entity.setNewsTitle(title);
		entity.setNewsDescription(description);
		entity.setPublisher(publisher);
		entity.setPublishedAt(publishedAt);

		try {
			newsDao.upsertNewsInfo(entity);
		} catch (Exception e) {
			log.warn("NEWS_INFO MERGE 실패 link={}: {}", articleLink, e.getMessage());
			return buildResponseWithoutId(title, description, publisher, entity.getArticleLink(), publishedAt);
		}

		Long id = newsDao.selectNewsInfoIdByLink(entity.getArticleLink());
		if (id == null) {
			return buildResponseWithoutId(title, description, publisher, entity.getArticleLink(), publishedAt);
		}

		if (stockCodeOrNull != null && !stockCodeOrNull.isBlank()) {
			try {
				newsDao.mergeNewsInfoStock(id, stockCodeOrNull.trim());
			} catch (Exception e) {
				log.warn("NEWS_INFO_STOCK MERGE 실패 newsInfoId={} stock={}", id, stockCodeOrNull, e);
			}
		}

		return new NewsResponse(
				id,
				title,
				description,
				publisher,
				entity.getArticleLink(),
				toInstant(publishedAt));
	}

	private static NewsResponse buildResponseWithoutId(
			String title,
			String description,
			String publisher,
			String articleLink,
			Date publishedAt) {
		return new NewsResponse(null, title, description, publisher, articleLink, toInstant(publishedAt));
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
					e.getArticleLink(),
					toInstant(e.getPublishedAt())));
		}
		return list;
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
		return Math.min(size, 100);
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
				return truncate(host, 100);
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

	private static String truncate(String s, int maxLen) {
		if (s == null) {
			return "";
		}
		if (s.length() <= maxLen) {
			return s;
		}
		return s.substring(0, maxLen);
	}
}
