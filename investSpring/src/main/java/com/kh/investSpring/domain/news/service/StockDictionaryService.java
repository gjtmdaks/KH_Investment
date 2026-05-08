package com.kh.investSpring.domain.news.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import jakarta.annotation.PostConstruct;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.kh.investSpring.domain.news.dao.NewsDao;
import com.kh.investSpring.domain.news.dto.StockDictionaryEntry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * STOCKS 테이블을 메모리 사전(종목명 → 종목코드)으로 적재해 뉴스 본문에서 관련 종목을 매칭할 때 사용.
 *
 * 오탐을 줄이기 위해 다음 항목은 사전에서 제외합니다
 * 
 *   종목명이 공백/null
 *   한글 1글자 종목명(부분 문자열 오탐 위험)
 *   일반어와 충돌이 잦은 짧은 종목명({@link #BLACKLIST_NAMES})
 * 
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockDictionaryService {

	/**
	 * 일반어/거시 키워드와 충돌이 잦은 종목명 화이트리스트형 차단 목록.
	 * - 영문 약어: KT/SK/LG/AK/DB/AI/KB는 본문에 일반어/거시 키워드로도 자주 등장
	 * - 한글 일반어: 한국/대한/중앙 등은 종목명이지만 보통 명사로 더 자주 사용
	 * - 거시 키워드와 동일: 다우/금/은행 등
	 */
	private static final Set<String> BLACKLIST_NAMES = Set.of(
			"KT", "SK", "LG", "AK", "DB", "AI", "KB", "DL",
			"한국", "대한", "중앙", "동양", "다우", "금", "은행",
			"한일", "신라", "유진", "다음", "아이"
	);

	/**
	 * 시드 데이터(예: 익스포트.xlsx)에서 종목명 끝에 붙는 비표준 접미사를 제거하기 위한 패턴.
	 *   {@code 삼성전자보통주} → {@code 삼성전자}
	 *   {@code 삼성전자1우선주} → {@code 삼성전자}
	 *   {@code 삼성전자우선주B} → {@code 삼성전자}
	 */
	private static final Pattern STOCK_NAME_SUFFIX = Pattern.compile("(?:보통주|[0-9]?우선주[A-Z]?)$");

	private final NewsDao newsDao;

	private final AtomicReference<List<Entry>> entriesRef = new AtomicReference<>(Collections.emptyList());

	public record Entry(String stockCode, String stockName, String normalizedName) {
	}

	@PostConstruct
	public void init() {
		try {
			reload();
		} catch (Exception e) {
			log.warn("종목 사전 초기 로드 실패: {}", e.getMessage());
		}
	}

	/** 1시간마다 STOCKS 테이블을 다시 읽어 사전을 갱신. */
	@Scheduled(fixedDelayString = "PT1H")
	public void reload() {
		List<StockDictionaryEntry> rows = safeSelectAll();
		List<Entry> filtered = filter(rows);
		entriesRef.set(Collections.unmodifiableList(filtered));
		log.info("종목 사전 갱신 완료: total={}, used={}", rows.size(), filtered.size());
	}

	public List<Entry> getEntries() {
		return entriesRef.get();
	}

	private List<StockDictionaryEntry> safeSelectAll() {
		try {
			List<StockDictionaryEntry> rows = newsDao.selectAllActiveStockDictionary();
			return rows == null ? List.of() : rows;
		} catch (Exception e) {
			log.warn("종목 사전 SELECT 실패: {}", e.getMessage());
			return List.of();
		}
	}

	/**
	 * 사전 적재 시 일괄 처리 내용
	 *   1. 비표준 접미사("보통주", "1우선주" 등) 제거 → canonical 종목명 추출
	 *   2. 같은 canonical에 보통주·우선주가 함께 등장하면 보통주를 우선 채택해 종목코드가 본주 쪽으로 매핑되게 함
	 *   3. 블랙리스트 / 길이 / 빈 문자열 검사
	 */
	private static List<Entry> filter(List<StockDictionaryEntry> rows) {
		Map<String, Entry> picked = new LinkedHashMap<>();
		Map<String, Boolean> pickedFromCommon = new LinkedHashMap<>();

		for (StockDictionaryEntry r : rows) {
			if (r == null) continue;
			String code = r.getStockCode();
			String rawName = r.getStockName();
			if (code == null || code.isBlank()) continue;
			if (rawName == null) continue;

			String trimmed = rawName.trim();
			if (trimmed.isEmpty()) continue;

			boolean isCommonStock = trimmed.endsWith("보통주");
			String canonical = stripStockNameSuffix(trimmed);

			if (canonical.isEmpty()) continue;
			if (BLACKLIST_NAMES.contains(canonical)) continue;
			if (!isMatchableName(canonical)) continue;

			Entry existing = picked.get(canonical);
			if (existing == null) {
				picked.put(canonical, new Entry(code.trim(), canonical, canonical.toLowerCase(Locale.ROOT)));
				pickedFromCommon.put(canonical, isCommonStock);
				continue;
			}
			// 이미 채택된 항목이 우선주(또는 접미사 미상)인데, 이번 행이 보통주이면 보통주로 교체
			boolean prevWasCommon = Boolean.TRUE.equals(pickedFromCommon.get(canonical));
			if (!prevWasCommon && isCommonStock) {
				picked.put(canonical, new Entry(code.trim(), canonical, canonical.toLowerCase(Locale.ROOT)));
				pickedFromCommon.put(canonical, true);
			}
		}

		return new ArrayList<>(picked.values());
	}

	/**
	 * 종목명 끝의 비표준 접미사("보통주", "1우선주", "우선주B" 등)를 제거.
	 * 매칭되지 않으면 입력값을 그대로 반환합니다.
	 */
	private static String stripStockNameSuffix(String name) {
		if (name == null) return "";
		String stripped = STOCK_NAME_SUFFIX.matcher(name).replaceFirst("");
		return stripped.trim();
	}

	/**
	 * 사전에 들이기에 안전한 종목명인지 판단.
	 *   한글이 한 글자라도 포함되어 있으면 전체 길이 ≥ 2자
	 *   한글이 없는 경우(영문/숫자 조합) 전체 길이 ≥ 2자
	 */
	private static boolean isMatchableName(String name) {
		if (name == null) return false;
		int len = name.length();
		if (len < 2) return false;
		return true;
	}
}
