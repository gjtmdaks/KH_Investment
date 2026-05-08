package com.kh.investSpring.domain.news.util;

/**
 * 네이버 검색 스니펫과 OG 메타를 병합할 때 사용하는 공통 로직입니다.
 */
public final class NewsContentMergeUtil {

	private NewsContentMergeUtil() {
	}

	public static String truncate(String s, int maxLen) {
		if (s == null) {
			return "";
		}
		if (s.length() <= maxLen) {
			return s;
		}
		return s.substring(0, maxLen);
	}

	/** 네이버 검색 제목이 말줄임이면 원문 OG 제목을 쓰고, 아니면 더 긴 쪽을 사용합니다. */
	public static String mergeTitle(String naverTitle, String ogTitle) {
		String n = naverTitle == null ? "" : naverTitle;
		if (ogTitle == null || ogTitle.isBlank()) {
			return truncate(n, 500);
		}
		String o = ogTitle.trim();
		if (o.length() < 4 && n.length() > o.length()) {
			return truncate(n, 500);
		}
		if (looksTruncated(n)) {
			return truncate(o, 500);
		}
		if (o.length() > n.length()) {
			return truncate(o, 500);
		}
		return truncate(n, 500);
	}

	public static String mergeDescription(String naverDesc, String ogDesc) {
		String n = naverDesc == null ? "" : naverDesc;
		if (ogDesc == null || ogDesc.isBlank()) {
			return truncate(n, 4000);
		}
		String o = ogDesc.trim();
		if (o.length() > n.length()) {
			return truncate(o, 4000);
		}
		return truncate(n, 4000);
	}

	public static boolean looksTruncated(String t) {
		if (t == null || t.isBlank()) {
			return false;
		}
		String s = t.trim();
		return s.endsWith("...") || s.endsWith("…") || s.endsWith("..");
	}
}
