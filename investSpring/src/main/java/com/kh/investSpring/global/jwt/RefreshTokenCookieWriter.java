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

	public void addCookie(HttpServletResponse response, String refreshToken) {
		long maxAgeSeconds = Math.max(1, refreshExpirationMs / 1000);
		ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, refreshToken)
				.httpOnly(true)
				.secure(refreshCookieSettings.secure())
				.path("/")
				.sameSite(refreshCookieSettings.sameSite())
				.maxAge(Duration.ofSeconds(maxAgeSeconds))
				.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}
}
