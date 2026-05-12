package com.kh.investSpring.global.jwt;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import com.kh.investSpring.global.config.RefreshCookieSettings;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthCookieClearer {

	private static final String SESSION_COOKIE_NAME = "JSESSIONID";

	private final RefreshCookieSettings refreshCookieSettings;

	@Value("${server.servlet.context-path:}")
	private String contextPath;

	public void clearAuthCookies(HttpServletResponse response) {
		for (String cookiePath : resolveCookiePaths()) {
			expireCookie(response, RefreshTokenCookieWriter.REFRESH_COOKIE_NAME, cookiePath);
			expireCookie(response, SESSION_COOKIE_NAME, cookiePath);
		}
	}

	private void expireCookie(HttpServletResponse response, String name, String cookiePath) {
		ResponseCookie cookie = ResponseCookie.from(name, "")
				.httpOnly(true)
				.secure(refreshCookieSettings.secure())
				.path(cookiePath)
				.sameSite(refreshCookieSettings.sameSite())
				.maxAge(Duration.ZERO)
				.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

		Cookie servletCookie = new Cookie(name, "");
		servletCookie.setPath(cookiePath);
		servletCookie.setMaxAge(0);
		servletCookie.setHttpOnly(true);
		servletCookie.setSecure(refreshCookieSettings.secure());
		response.addCookie(servletCookie);
	}

	private List<String> resolveCookiePaths() {
		String contextCookiePath = resolveContextCookiePath();
		if ("/".equals(contextCookiePath)) {
			return List.of("/");
		}
		return List.of(contextCookiePath, "/");
	}

	private String resolveContextCookiePath() {
		if (contextPath == null || contextPath.isBlank()) {
			return "/";
		}
		return contextPath.startsWith("/") ? contextPath : "/" + contextPath;
	}
}
