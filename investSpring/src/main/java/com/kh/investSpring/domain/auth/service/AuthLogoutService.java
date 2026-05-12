package com.kh.investSpring.domain.auth.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.kh.investSpring.domain.auth.dto.LogoutResponse;
import com.kh.investSpring.global.jwt.AuthCookieClearer;

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

	@Value("${spring.security.oauth2.client.registration.kakao.client-id:}")
	private String kakaoClientId;

	@Value("${app.oauth2.kakao-logout-redirect-uri:}")
	private String kakaoLogoutRedirectUri;

	public LogoutResponse logout(HttpServletRequest request, HttpServletResponse response) {
		SecurityContextHolder.clearContext();

		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}

		authCookieClearer.clearAuthCookies(response);

		return new LogoutResponse(buildKakaoLogoutUrl(request));
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
