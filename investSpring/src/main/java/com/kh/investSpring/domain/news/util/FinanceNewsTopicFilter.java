package com.kh.investSpring.domain.news.util;

import com.kh.investSpring.api.naver.dto.NaverNewsItemDto;
import com.kh.investSpring.global.util.HtmlStripUtil;

/**
 * 시황/종목 뉴스에서 연예·예능 등 비(非)금융 주제를 걸러 냅니다.
 * 네이버 뉴스 API는 카테고리 필터가 없어 검색어 + 사후 필터로 보완합니다.
 */
public final class FinanceNewsTopicFilter {

	private FinanceNewsTopicFilter() {
	}

	/**
	 * 시황용 검색어: 증시·경제·기업 키워드가 동시에 걸리도록 AND 조합(네이버 뉴스 검색 규칙에 맞춤).
	 */
	public static final String MARKET_SEARCH_QUERY = "증시 증권 경제 기업";

	private static final String[] NOISE_KEYWORDS = {
			"연예", "열애", "이혼", "결혼식", "웨딩",
			"드라마", "예능", "아이돌", "가요", "리메이크",
			"영화", "배우", "뮤지컬", "콘서트", "티켓",
			"웹툰", "만화", "애니",
			"미스터트롯", "미스트롯", "아이돌 패밀리",
			"화제의 인물", "소속사",
			"프로야구", "KBO", "프로축구", "K리그1", "해외축구",
			"배틀그라운드", "e스포츠", "게임 업데이트",
			"신작 출시", "먹방", "브이로그",
	};

	public static boolean passesNaverItem(NaverNewsItemDto item) {
		if (item == null) {
			return false;
		}
		String title = HtmlStripUtil.stripHtml(item.title());
		String desc = HtmlStripUtil.stripHtml(item.description());
		return passesText(title, desc);
	}

	public static boolean passesText(String title, String description) {
		String combined = ((title != null ? title : "") + " " + (description != null ? description : ""))
				.toLowerCase();
		if (combined.isBlank()) {
			return false;
		}
		for (String noise : NOISE_KEYWORDS) {
			if (combined.contains(noise.toLowerCase())) {
				return false;
			}
		}
		return true;
	}
}
