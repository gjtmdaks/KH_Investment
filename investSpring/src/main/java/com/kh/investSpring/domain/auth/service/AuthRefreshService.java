package com.kh.investSpring.domain.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.kh.investSpring.domain.auth.dto.RefreshTokenClaims;
import com.kh.investSpring.global.jwt.AuthCookieClearer;
import com.kh.investSpring.global.jwt.JwtTokenProvider;
import com.kh.investSpring.global.jwt.RefreshTokenCookieWriter;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthRefreshService {

	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenStore refreshTokenStore;
	private final AuthTokenIssueService authTokenIssueService;
	private final AuthCookieClearer authCookieClearer;

	public void refresh(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = readRefreshTokenCookie(request);
		if (!StringUtils.hasText(refreshToken)) {
			clearAndDeny(response, null);
			return;
		}

		RefreshTokenClaims claims;
		try {
			claims = jwtTokenProvider.parseRefreshToken(refreshToken);
		} catch (Exception e) {
			clearAndDeny(response, null);
			return;
		}

		Long userNo = claims.userNo();
		String presentedJti = claims.jti();
		String storedJti = refreshTokenStore.getJti(userNo);

		if (!StringUtils.hasText(storedJti) || !storedJti.equals(presentedJti)) {
			clearAndDeny(response, userNo);
			return;
		}

		authTokenIssueService.issue(userNo, response);
	}

	private void clearAndDeny(HttpServletResponse response, Long userNo) {
		if (userNo != null) {
			refreshTokenStore.revoke(userNo);
		}
		authCookieClearer.clearAuthCookies(response);
		throw new IllegalArgumentException("Refresh token이 유효하지 않습니다.");
	}

	private static String readRefreshTokenCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}
		for (Cookie cookie : cookies) {
			if (RefreshTokenCookieWriter.REFRESH_COOKIE_NAME.equals(cookie.getName())) {
				String v = cookie.getValue();
				return v != null && !v.isBlank() ? v : null;
			}
		}
		return null;
	}
}

