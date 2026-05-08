package com.kh.investSpring.domain.news.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 뉴스 제목/요약에서 대표 키워드(primary_label)와 키워드 종류(keyword_kind)를 결정합니다.
 *
 * 점수 규칙:
 * - 종목(STOCK) 키워드 기본 10점
 * - 업종/테마(SECTOR) 키워드 기본 5점
 * - 거시(MACRO) 키워드 기본 3점
 * - 이슈(ISSUE) 키워드 기본 3점
 * - 제목(title)에 포함되면 해당 키워드 점수는 2배(가산)
 * - description 포함 점수 + title 포함 점수(2배)를 합산하여 비교
 */
@Service
@Slf4j
public class NewsKeywordLabelService {

	public static final String KIND_STOCK = "STOCK";
	public static final String KIND_SECTOR = "SECTOR";
	public static final String KIND_MACRO = "MACRO";
	public static final String KIND_ISSUE = "ISSUE";

	private static final int W_STOCK = 10;
	private static final int W_SECTOR = 5;
	private static final int W_MACRO = 3;
	private static final int W_ISSUE = 3;

	private static final int KIND_PRIORITY_STOCK = 4;
	private static final int KIND_PRIORITY_SECTOR = 3;
	private static final int KIND_PRIORITY_MACRO = 2;
	private static final int KIND_PRIORITY_ISSUE = 1;

	/**
	 * 키워드 테이블(가중치)
	 * - LinkedHashMap: 동점 시 먼저 등록된 키워드가 우선권을 가짐(결정 규칙 고정)
	 */
	private static final Map<String, KeywordMeta> KEYWORDS = new LinkedHashMap<>();

	/**
	 * 별칭 테이블(alias -> canonical).
	 * - 예: "삼전" -> "삼성전자"
	 *
	 * 주의: static 초기화 순서상 static 블록에서 alias()를 호출하므로,
	 * ALIASES는 static 블록보다 앞에서 초기화되어야 합니다.
	 */
	private static final Map<String, String> ALIASES = new LinkedHashMap<>();

	static {
		// STOCK (대표 종목명 위주 - 필요 시 계속 확장)
		put("삼성전자", KIND_STOCK, W_STOCK);
		alias("삼성 전자", "삼성전자");
		alias("삼전", "삼성전자");
		put("SK하이닉스", KIND_STOCK, W_STOCK);
		alias("sk하이닉스", "SK하이닉스");
		alias("하이닉스", "SK하이닉스");
		put("현대차", KIND_STOCK, W_STOCK);
		alias("현대자동차", "현대차");
		put("기아", KIND_STOCK, W_STOCK);
		alias("기아차", "기아");
		put("LG에너지솔루션", KIND_STOCK, W_STOCK);
		alias("lg엔솔", "LG에너지솔루션");
		put("삼성SDI", KIND_STOCK, W_STOCK);
		alias("삼성 sdi", "삼성SDI");
		put("LG화학", KIND_STOCK, W_STOCK);
		put("포스코홀딩스", KIND_STOCK, W_STOCK);
		alias("posco홀딩스", "포스코홀딩스");
		alias("포스코", "포스코홀딩스");
		put("네이버", KIND_STOCK, W_STOCK);
		alias("NAVER", "네이버");
		put("카카오", KIND_STOCK, W_STOCK);
		put("셀트리온", KIND_STOCK, W_STOCK);
		put("삼성바이오로직스", KIND_STOCK, W_STOCK);
		alias("삼바", "삼성바이오로직스");
		alias("삼성 바이오로직스", "삼성바이오로직스");
		put("KB금융", KIND_STOCK, W_STOCK);
		alias("kb금융지주", "KB금융");
		put("신한지주", KIND_STOCK, W_STOCK);
		alias("신한금융", "신한지주");
		put("하나금융", KIND_STOCK, W_STOCK);
		alias("하나금융지주", "하나금융");
		put("삼성생명", KIND_STOCK, W_STOCK);
		put("삼성화재", KIND_STOCK, W_STOCK);
		put("한국전력", KIND_STOCK, W_STOCK);
		alias("한전", "한국전력");
		put("현대모비스", KIND_STOCK, W_STOCK);
		put("LG전자", KIND_STOCK, W_STOCK);
		put("삼성전기", KIND_STOCK, W_STOCK);
		put("삼성물산", KIND_STOCK, W_STOCK);
		put("KT", KIND_STOCK, W_STOCK);
		alias("케이티", "KT");
		put("SK텔레콤", KIND_STOCK, W_STOCK);
		alias("SKT", "SK텔레콤");
		put("한화에어로스페이스", KIND_STOCK, W_STOCK);
		put("한화오션", KIND_STOCK, W_STOCK);
		alias("대우조선해양", "한화오션");
		put("HD현대중공업", KIND_STOCK, W_STOCK);
		alias("현대중공업", "HD현대중공업");
		put("대한항공", KIND_STOCK, W_STOCK);
		put("아모레퍼시픽", KIND_STOCK, W_STOCK);

		// SECTOR (업종/테마)
		put("반도체", KIND_SECTOR, W_SECTOR);
		put("이차전지", KIND_SECTOR, W_SECTOR);
		put("2차전지", KIND_SECTOR, W_SECTOR);
		put("전기차", KIND_SECTOR, W_SECTOR);
		put("배터리", KIND_SECTOR, W_SECTOR);
		put("자동차", KIND_SECTOR, W_SECTOR);
		put("조선", KIND_SECTOR, W_SECTOR);
		put("방산", KIND_SECTOR, W_SECTOR);
		put("바이오", KIND_SECTOR, W_SECTOR);
		put("제약", KIND_SECTOR, W_SECTOR);
		put("헬스케어", KIND_SECTOR, W_SECTOR);
		put("게임", KIND_SECTOR, W_SECTOR);
		put("인터넷", KIND_SECTOR, W_SECTOR);
		put("AI", KIND_SECTOR, W_SECTOR);
		put("인공지능", KIND_SECTOR, W_SECTOR);
		alias("a.i.", "AI");
		put("클라우드", KIND_SECTOR, W_SECTOR);
		put("데이터센터", KIND_SECTOR, W_SECTOR);
		put("로봇", KIND_SECTOR, W_SECTOR);
		put("원전", KIND_SECTOR, W_SECTOR);
		put("태양광", KIND_SECTOR, W_SECTOR);
		put("풍력", KIND_SECTOR, W_SECTOR);
		put("신재생", KIND_SECTOR, W_SECTOR);
		put("수소", KIND_SECTOR, W_SECTOR);
		put("정유", KIND_SECTOR, W_SECTOR);
		put("화학", KIND_SECTOR, W_SECTOR);
		put("철강", KIND_SECTOR, W_SECTOR);
		put("건설", KIND_SECTOR, W_SECTOR);
		put("리츠", KIND_SECTOR, W_SECTOR);
		put("은행", KIND_SECTOR, W_SECTOR);
		put("증권", KIND_SECTOR, W_SECTOR);
		put("보험", KIND_SECTOR, W_SECTOR);
		put("소비재", KIND_SECTOR, W_SECTOR);
		put("유통", KIND_SECTOR, W_SECTOR);
		put("면세", KIND_SECTOR, W_SECTOR);
		put("여행", KIND_SECTOR, W_SECTOR);
		put("항공", KIND_SECTOR, W_SECTOR);
		put("해운", KIND_SECTOR, W_SECTOR);
		put("콘텐츠", KIND_SECTOR, W_SECTOR);
		put("엔터", KIND_SECTOR, W_SECTOR);

		// MACRO (거시경제)
		put("금리", KIND_MACRO, W_MACRO);
		put("환율", KIND_MACRO, W_MACRO);
		put("달러", KIND_MACRO, W_MACRO);
		put("원/달러", KIND_MACRO, W_MACRO);
		put("엔화", KIND_MACRO, W_MACRO);
		put("위안", KIND_MACRO, W_MACRO);
		put("인플레이션", KIND_MACRO, W_MACRO);
		put("물가", KIND_MACRO, W_MACRO);
		put("소비자물가", KIND_MACRO, W_MACRO);
		put("생산자물가", KIND_MACRO, W_MACRO);
		put("GDP", KIND_MACRO, W_MACRO);
		put("성장률", KIND_MACRO, W_MACRO);
		put("고용", KIND_MACRO, W_MACRO);
		put("실업", KIND_MACRO, W_MACRO);
		put("수출", KIND_MACRO, W_MACRO);
		put("무역", KIND_MACRO, W_MACRO);
		put("경상수지", KIND_MACRO, W_MACRO);
		put("국채", KIND_MACRO, W_MACRO);
		put("채권", KIND_MACRO, W_MACRO);
		put("국고채", KIND_MACRO, W_MACRO);
		put("기준금리", KIND_MACRO, W_MACRO);
		put("금리인하", KIND_MACRO, W_MACRO);
		put("금리인상", KIND_MACRO, W_MACRO);
		put("FOMC", KIND_MACRO, W_MACRO);
		put("연준", KIND_MACRO, W_MACRO);
		put("Fed", KIND_MACRO, W_MACRO);
		put("CPI", KIND_MACRO, W_MACRO);
		put("PPI", KIND_MACRO, W_MACRO);
		put("유가", KIND_MACRO, W_MACRO);
		put("원유", KIND_MACRO, W_MACRO);
		put("WTI", KIND_MACRO, W_MACRO);
		put("브렌트", KIND_MACRO, W_MACRO);
		put("금", KIND_MACRO, W_MACRO);
		put("비트코인", KIND_MACRO, W_MACRO);
		put("가상자산", KIND_MACRO, W_MACRO);
		put("코스피", KIND_MACRO, W_MACRO);
		put("코스닥", KIND_MACRO, W_MACRO);
		put("나스닥", KIND_MACRO, W_MACRO);
		put("S&P", KIND_MACRO, W_MACRO);
		put("다우", KIND_MACRO, W_MACRO);
		put("증시", KIND_MACRO, W_MACRO);
		put("경제", KIND_MACRO, W_MACRO);

		// ISSUE (이벤트/이슈 키워드)
		put("실적", KIND_ISSUE, W_ISSUE);
		put("정책", KIND_ISSUE, W_ISSUE);
		put("어닝", KIND_ISSUE, W_ISSUE);
		put("어닝쇼크", KIND_ISSUE, W_ISSUE);
		put("가이던스", KIND_ISSUE, W_ISSUE);
		put("상향", KIND_ISSUE, W_ISSUE);
		put("하향", KIND_ISSUE, W_ISSUE);
		put("공급", KIND_ISSUE, W_ISSUE);
		put("수요", KIND_ISSUE, W_ISSUE);
		put("수주", KIND_ISSUE, W_ISSUE);
		put("계약", KIND_ISSUE, W_ISSUE);
		put("M&A", KIND_ISSUE, W_ISSUE);
		put("인수", KIND_ISSUE, W_ISSUE);
		put("합병", KIND_ISSUE, W_ISSUE);
		put("분할", KIND_ISSUE, W_ISSUE);
		put("IPO", KIND_ISSUE, W_ISSUE);
		put("상장", KIND_ISSUE, W_ISSUE);
		put("유상증자", KIND_ISSUE, W_ISSUE);
		put("무상증자", KIND_ISSUE, W_ISSUE);
		put("자사주", KIND_ISSUE, W_ISSUE);
		put("배당", KIND_ISSUE, W_ISSUE);
		put("리콜", KIND_ISSUE, W_ISSUE);
		put("파업", KIND_ISSUE, W_ISSUE);
		put("규제", KIND_ISSUE, W_ISSUE);
		put("제재", KIND_ISSUE, W_ISSUE);
		put("관세", KIND_ISSUE, W_ISSUE);
		put("보조금", KIND_ISSUE, W_ISSUE);
		put("전쟁", KIND_ISSUE, W_ISSUE);
		put("중동", KIND_ISSUE, W_ISSUE);
		put("우크라이나", KIND_ISSUE, W_ISSUE);
		put("중국", KIND_ISSUE, W_ISSUE);
		put("미국", KIND_ISSUE, W_ISSUE);
		put("일본", KIND_ISSUE, W_ISSUE);
	}

	private static void alias(String alias, String canonical) {
		if (alias == null || canonical == null) {
			return;
		}
		String a = alias.trim().toLowerCase(Locale.ROOT);
		String c = canonical.trim();
		if (!a.isBlank() && !c.isBlank()) {
			ALIASES.put(a, c);
		}
	}

	private static void put(String keyword, String kind, int weight) {
		KEYWORDS.put(keyword, new KeywordMeta(kind, weight, keyword.trim().toLowerCase(Locale.ROOT)));
	}

	public record PrimaryKeyword(String primaryLabel, String keywordKind, int score) {
	}

	private record KeywordMeta(String kind, int weight, String normalizedKeyword) {
	}

	private record Candidate(String canonicalKeyword, String kind, int score, int kindPriority) {
	}

	// ---- 진단(통계) ----
	private final AtomicLong totalDetections = new AtomicLong(0);
	private final ConcurrentHashMap<String, LongAdder> labelCounts = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, LongAdder> labelScores = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, LongAdder> kindCounts = new ConcurrentHashMap<>();

	/**
	 * @param title 기사 제목(plain text)
	 * @param description 기사 요약(plain text)
	 * @param extraStockKeyword 종목 뉴스 수집 시 종목명/코드 등 추가 후보(없으면 null/blank)
	 */
	public PrimaryKeyword detectPrimaryKeyword(String title, String description, String extraStockKeyword) {
		String t = normalize(title);
		String d = normalize(description);

		Candidate best = null;

		// 1) 동적 종목 후보(가장 우선 평가)
		if (extraStockKeyword != null && !extraStockKeyword.isBlank()) {
			String k = extraStockKeyword.trim();
			String canonical = canonicalize(k);
			int score = scoreOne(normalizeKeyword(canonical), W_STOCK, t, d);
			best = betterOf(best, new Candidate(canonical, KIND_STOCK, score, kindPriority(KIND_STOCK)));
		}

		// 2) 정적 테이블 기반 키워드 평가
		for (Map.Entry<String, KeywordMeta> e : KEYWORDS.entrySet()) {
			String keyword = e.getKey();
			KeywordMeta meta = e.getValue();
			int score = scoreOne(meta.normalizedKeyword, meta.weight, t, d);
			best = betterOf(best, new Candidate(keyword, meta.kind, score, kindPriority(meta.kind)));
		}

		if (best == null || best.score <= 0 || best.canonicalKeyword == null || best.canonicalKeyword.isBlank()) {
			return null;
		}

		recordStats(best.canonicalKeyword, best.kind, best.score);
		return new PrimaryKeyword(best.canonicalKeyword, best.kind, best.score);
	}

	private static Candidate betterOf(Candidate currentBest, Candidate challenger) {
		if (challenger == null || challenger.score <= 0) {
			return currentBest;
		}
		if (currentBest == null || currentBest.score <= 0) {
			return challenger;
		}
		if (challenger.score > currentBest.score) {
			return challenger;
		}
		if (challenger.score < currentBest.score) {
			return currentBest;
		}
		// 점수 동점이면 keyword_kind 우선순위(STOCK > SECTOR > MACRO > ISSUE)로 결정
		if (challenger.kindPriority > currentBest.kindPriority) {
			return challenger;
		}
		return currentBest;
	}

	private static int kindPriority(String kind) {
		if (KIND_STOCK.equals(kind)) {
			return KIND_PRIORITY_STOCK;
		}
		if (KIND_SECTOR.equals(kind)) {
			return KIND_PRIORITY_SECTOR;
		}
		if (KIND_MACRO.equals(kind)) {
			return KIND_PRIORITY_MACRO;
		}
		return KIND_PRIORITY_ISSUE;
	}

	private static String canonicalize(String rawKeyword) {
		if (rawKeyword == null) {
			return "";
		}
		String norm = rawKeyword.trim().toLowerCase(Locale.ROOT);
		String mapped = ALIASES.get(norm);
		if (mapped != null && !mapped.isBlank()) {
			return mapped;
		}
		return rawKeyword.trim();
	}

	private static String normalizeKeyword(String keyword) {
		return keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
	}

	private static int scoreOne(String keywordNorm, int baseWeight, String titleNorm, String descNorm) {
		if (keywordNorm == null || keywordNorm.isBlank()) {
			return 0;
		}

		int score = 0;
		if (!descNorm.isBlank() && descNorm.contains(keywordNorm)) {
			score += baseWeight;
		}
		if (!titleNorm.isBlank() && titleNorm.contains(keywordNorm)) {
			score += baseWeight * 2;
		}
		return score;
	}

	private static String normalize(String s) {
		if (s == null) {
			return "";
		}
		String out = s.trim().toLowerCase(Locale.ROOT);
		out = out.replace('\u00A0', ' '); // nbsp
		out = out.replaceAll("\\s+", " ");
		return out;
	}

	private void recordStats(String label, String kind, int score) {
		totalDetections.incrementAndGet();
		labelCounts.computeIfAbsent(label, k -> new LongAdder()).increment();
		labelScores.computeIfAbsent(label, k -> new LongAdder()).add(score);
		kindCounts.computeIfAbsent(kind, k -> new LongAdder()).increment();
	}

	/**
	 * 오탐/튜닝을 위해 상위 라벨/스코어 통계를 주기적으로 로그로 출력합니다.
	 * - 5분 간격(기본), 최근 누적 통계 기반
	 */
	@Scheduled(fixedDelayString = "PT5M")
	public void logTopStats() {
		long total = totalDetections.get();
		if (total <= 0) {
			return;
		}

		ArrayList<Map.Entry<String, LongAdder>> labels = new ArrayList<>(labelCounts.entrySet());
		labels.sort(Comparator.comparingLong((Map.Entry<String, LongAdder> e) -> e.getValue().sum()).reversed());

		StringBuilder sb = new StringBuilder(512);
		sb.append("NewsKeywordLabel 통계 total=").append(total).append(" ");
		sb.append("kinds=");
		sb.append(kindCounts.entrySet().stream()
				.sorted(Comparator.comparing(e -> e.getKey()))
				.map(e -> e.getKey() + ":" + e.getValue().sum())
				.reduce((a, b) -> a + ", " + b).orElse("-"));
		sb.append(" topLabels=");

		int limit = Math.min(10, labels.size());
		for (int i = 0; i < limit; i++) {
			String label = labels.get(i).getKey();
			long cnt = labels.get(i).getValue().sum();
			long sumScore = labelScores.getOrDefault(label, new LongAdder()).sum();
			long avg = cnt <= 0 ? 0 : Math.round((double) sumScore / (double) cnt);
			if (i > 0) sb.append(" | ");
			sb.append(label).append("(").append(cnt).append(",avg=").append(avg).append(")");
		}

		log.info(sb.toString());
	}
}

