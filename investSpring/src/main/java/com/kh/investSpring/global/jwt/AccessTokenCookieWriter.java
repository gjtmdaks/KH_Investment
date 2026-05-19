package com.kh.investSpring.global.jwt;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import com.kh.investSpring.global.config.RefreshCookieSettings;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AccessTokenCookieWriter {

	public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";

	@Value("${jwt.access-expiration-ms:3600000}")
	private long accessExpirationMs;

	private final RefreshCookieSettings refreshCookieSettings;

	@Value("${server.servlet.context-path:}")
	private String contextPath;

	public void addCookie(HttpServletResponse response, String accessToken) {
		long maxAgeSeconds = Math.max(1, accessExpirationMs / 1000);
		ResponseCookie cookie = buildCookie(accessToken, Duration.ofSeconds(maxAgeSeconds));
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	private ResponseCookie buildCookie(String value, Duration maxAge) {
		return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, value)
				.httpOnly(true)
				.secure(refreshCookieSettings.secure())
				.path(resolveCookiePath())
				.sameSite(refreshCookieSettings.sameSite())
				.maxAge(maxAge)
				.build();
	}

	private String resolveCookiePath() {
		if (contextPath == null || contextPath.isBlank()) {
			return "/";
		}
		return contextPath.startsWith("/") ? contextPath : "/" + contextPath;
	}
}
