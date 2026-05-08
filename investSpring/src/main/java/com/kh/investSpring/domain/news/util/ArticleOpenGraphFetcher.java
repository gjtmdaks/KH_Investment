package com.kh.investSpring.domain.news.util;

import java.net.URI;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.kh.investSpring.global.util.HtmlStripUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 기사 원문 URL의 Open Graph / Twitter / 일반 description 메타를 읽어
 * 네이버 검색 API가 짧게 주는 제목·요약을 보강할 때 사용합니다.
 */
@Component
@Slf4j
public class ArticleOpenGraphFetcher {

	private static final String CHROME_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
			+ "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

	private static final Pattern OG_TITLE_1 = Pattern.compile(
			"<meta\\s[^>]*?property\\s*=\\s*[\"']og:title[\"'][^>]*?content\\s*=\\s*[\"']([^\"']*)[\"']",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private static final Pattern OG_TITLE_2 = Pattern.compile(
			"<meta\\s[^>]*?content\\s*=\\s*[\"']([^\"']*)[\"'][^>]*?property\\s*=\\s*[\"']og:title[\"']",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private static final Pattern TW_TITLE_1 = Pattern.compile(
			"<meta\\s[^>]*?name\\s*=\\s*[\"']twitter:title[\"'][^>]*?content\\s*=\\s*[\"']([^\"']*)[\"']",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private static final Pattern TW_TITLE_2 = Pattern.compile(
			"<meta\\s[^>]*?content\\s*=\\s*[\"']([^\"']*)[\"'][^>]*?name\\s*=\\s*[\"']twitter:title[\"']",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	private static final Pattern OG_DESC_1 = Pattern.compile(
			"<meta\\s[^>]*?property\\s*=\\s*[\"']og:description[\"'][^>]*?content\\s*=\\s*[\"']([^\"']*)[\"']",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private static final Pattern OG_DESC_2 = Pattern.compile(
			"<meta\\s[^>]*?content\\s*=\\s*[\"']([^\"']*)[\"'][^>]*?property\\s*=\\s*[\"']og:description[\"']",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private static final Pattern TW_DESC_1 = Pattern.compile(
			"<meta\\s[^>]*?name\\s*=\\s*[\"']twitter:description[\"'][^>]*?content\\s*=\\s*[\"']([^\"']*)[\"']",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private static final Pattern TW_DESC_2 = Pattern.compile(
			"<meta\\s[^>]*?content\\s*=\\s*[\"']([^\"']*)[\"'][^>]*?name\\s*=\\s*[\"']twitter:description[\"']",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private static final Pattern NAME_DESC_1 = Pattern.compile(
			"<meta\\s[^>]*?name\\s*=\\s*[\"']description[\"'][^>]*?content\\s*=\\s*[\"']([^\"']*)[\"']",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private static final Pattern NAME_DESC_2 = Pattern.compile(
			"<meta\\s[^>]*?content\\s*=\\s*[\"']([^\"']*)[\"'][^>]*?name\\s*=\\s*[\"']description[\"']",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	public record OgPayload(String title, String description) {
		public static OgPayload empty() {
			return new OgPayload(null, null);
		}
	}

	private final WebClient webClient;

	public ArticleOpenGraphFetcher() {
		this.webClient = WebClient.builder()
				.exchangeStrategies(ExchangeStrategies.builder()
						.codecs(c -> c.defaultCodecs().maxInMemorySize(700 * 1024))
						.build())
				.defaultHeader(HttpHeaders.USER_AGENT, CHROME_UA)
				.defaultHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE + ",application/xhtml+xml;q=0.9,*/*;q=0.8")
				.build();
	}

	public OgPayload fetch(String url) {
		if (url == null || url.isBlank()) {
			return OgPayload.empty();
		}
		final URI uri;
		try {
			uri = URI.create(url.trim());
		} catch (Exception e) {
			return OgPayload.empty();
		}
		String scheme = uri.getScheme();
		if (scheme == null
				|| (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
			return OgPayload.empty();
		}
		try {
			String html = webClient.get()
					.uri(uri)
					.retrieve()
					.bodyToMono(String.class)
					.timeout(Duration.ofMillis(2200))
					.block();
			if (html == null || html.isEmpty()) {
				return OgPayload.empty();
			}
			if (html.length() > 450_000) {
				html = html.substring(0, 450_000);
			}
			return parsePayload(html);
		} catch (Exception e) {
			log.debug("article OG fetch failed url={}: {}", url, e.getMessage());
			return OgPayload.empty();
		}
	}

	private OgPayload parsePayload(String html) {
		String title = firstNonBlank(
				findOne(OG_TITLE_1, html),
				findOne(OG_TITLE_2, html),
				findOne(TW_TITLE_1, html),
				findOne(TW_TITLE_2, html));
		String description = firstNonBlank(
				findOne(OG_DESC_1, html),
				findOne(OG_DESC_2, html),
				findOne(TW_DESC_1, html),
				findOne(TW_DESC_2, html),
				findOne(NAME_DESC_1, html),
				findOne(NAME_DESC_2, html));

		String cleanTitle = title == null || title.isBlank() ? null : HtmlStripUtil.stripHtml(title);
		String cleanDesc = description == null || description.isBlank() ? null : HtmlStripUtil.stripHtml(description);
		if (cleanTitle != null && cleanTitle.isBlank()) {
			cleanTitle = null;
		}
		if (cleanDesc != null && cleanDesc.isBlank()) {
			cleanDesc = null;
		}
		return new OgPayload(cleanTitle, cleanDesc);
	}

	private static String findOne(Pattern p, String html) {
		Matcher m = p.matcher(html);
		if (m.find()) {
			return m.group(1);
		}
		return null;
	}

	@SafeVarargs
	private static String firstNonBlank(String... candidates) {
		if (candidates == null) {
			return null;
		}
		for (String s : candidates) {
			if (s != null && !s.isBlank()) {
				return s;
			}
		}
		return null;
	}
}
