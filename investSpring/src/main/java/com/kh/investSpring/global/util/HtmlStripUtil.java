package com.kh.investSpring.global.util;

/**
 * 네이버 뉴스 API 등에서 내려오는 제목·요.summary에 포함된 HTML 태그를 제거합니다.
 */
public final class HtmlStripUtil {

	private HtmlStripUtil() {
	}

	public static String stripHtml(String raw) {
		if (raw == null || raw.isEmpty()) {
			return "";
		}
		String noTags = raw.replaceAll("<[^>]+>", " ");
		return decodeBasicEntities(noTags).replaceAll("\\s+", " ").trim();
	}

	private static String decodeBasicEntities(String s) {
		return s.replace("&nbsp;", " ")
				.replace("&amp;", "&")
				.replace("&lt;", "<")
				.replace("&gt;", ">")
				.replace("&quot;", "\"")
				.replace("&#39;", "'");
	}
}
