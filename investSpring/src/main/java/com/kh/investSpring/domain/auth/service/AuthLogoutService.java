package com.kh.investSpring.domain.auth.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.kh.investSpring.domain.auth.dto.LogoutResponse;
import com.kh.investSpring.domain.auth.dto.RefreshTokenClaims;
import com.kh.investSpring.global.jwt.AuthCookieClearer;
import com.kh.investSpring.global.jwt.JwtTokenProvider;
import com.kh.investSpring.global.jwt.RefreshTokenCookieWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class AuthLogoutService {

	private static final String KAKAO_LOGOUT_URL = "https://kauth.kakao.com/oauth/logout";

	private final AuthCookieClearer authCookieClearer;
	private final RefreshTokenStore refreshTokenStore;
	private final JwtTokenProvider jwtTokenProvider;

	@Value("${spring.security.oauth2.client.registration.kakao.client-id:}")
	private String kakaoClientId;

	@Value("${app.oauth2.kakao-logout-redirect-uri:}")
	private String kakaoLogoutRedirectUri;

	public LogoutResponse logout(HttpServletRequest request, HttpServletResponse response) {
		Long userNo = resolveUserNoBeforeClear(request);
		if (userNo != null) {
			refreshTokenStore.revoke(userNo);
		}

		SecurityContextHolder.clearContext();

		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}

		authCookieClearer.clearAuthCookies(response);

		return new LogoutResponse(buildKakaoLogoutUrl(request));
	}

	private Long resolveUserNoBeforeClear(HttpServletRequest request) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			Object principal = authentication.getPrincipal();
			if (principal instanceof Long l) {
				return l;
			}
			if (principal instanceof Integer i) {
				return i.longValue();
			}
			if (principal instanceof String s && StringUtils.hasText(s)) {
				try {
					return Long.parseLong(s);
				} catch (NumberFormatException ignore) {
				}
			}
		}

		String refreshToken = readRefreshTokenCookie(request);
		if (!StringUtils.hasText(refreshToken)) {
			return null;
		}

		try {
			RefreshTokenClaims claims = jwtTokenProvider.parseRefreshToken(refreshToken);
			return claims.userNo();
		} catch (Exception ignore) {
			return null;
		}
	}

	private static String readRefreshTokenCookie(HttpServletRequest request) {
		if (request.getCookies() == null) {
			return null;
		}
		for (var cookie : request.getCookies()) {
			if (RefreshTokenCookieWriter.REFRESH_COOKIE_NAME.equals(cookie.getName())) {
				String v = cookie.getValue();
				return v != null && !v.isBlank() ? v : null;
			}
		}
		return null;
	}

	private String buildKakaoLogoutUrl(HttpServletRequest request) {
		if (kakaoClientId == null || kakaoClientId.isBlank()) {
			return null;
		}

		String redirectUri = resolveKakaoLogoutRedirectUri(request);
		String encodedClientId = URLEncoder.encode(kakaoClientId, StandardCharsets.UTF_8);
		String encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);

		return KAKAO_LOGOUT_URL + "?client_id=" + encodedClientId + "&logout_redirect_uri=" + encodedRedirectUri;
	}

	private String resolveKakaoLogoutRedirectUri(HttpServletRequest request) {
		if (kakaoLogoutRedirectUri != null && !kakaoLogoutRedirectUri.isBlank()) {
			return kakaoLogoutRedirectUri;
		}

		return ServletUriComponentsBuilder.fromContextPath(request)
				.path("/logout/oauth2/code/kakao")
				.build()
				.toUriString();
	}
}
