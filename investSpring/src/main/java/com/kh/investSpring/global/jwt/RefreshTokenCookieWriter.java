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
public class RefreshTokenCookieWriter {

	public static final String REFRESH_COOKIE_NAME = "refreshToken";

	@Value("${jwt.refresh-expiration-ms:604800000}")
	private long refreshExpirationMs;

	private final RefreshCookieSettings refreshCookieSettings;

	@Value("${server.servlet.context-path:}")
	private String contextPath;

	public void addCookie(HttpServletResponse response, String refreshToken) {
		long maxAgeSeconds = Math.max(1, refreshExpirationMs / 1000);
		ResponseCookie cookie = buildCookie(refreshToken, Duration.ofSeconds(maxAgeSeconds));
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	private ResponseCookie buildCookie(String value, Duration maxAge) {
		return ResponseCookie.from(REFRESH_COOKIE_NAME, value)
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
