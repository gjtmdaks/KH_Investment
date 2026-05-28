package com.kh.investSpring.domain.auth.service;

import org.springframework.stereotype.Service;

import com.kh.investSpring.global.jwt.AccessTokenCookieWriter;
import com.kh.investSpring.global.jwt.JwtTokenProvider;
import com.kh.investSpring.global.jwt.RefreshTokenCookieWriter;
import com.kh.investSpring.global.jwt.RefreshTokenIssue;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthTokenIssueService {

	private final JwtTokenProvider jwtTokenProvider;
	private final AccessTokenCookieWriter accessTokenCookieWriter;
	private final RefreshTokenCookieWriter refreshTokenCookieWriter;
	private final RefreshTokenStore refreshTokenStore;

	public void issue(Long userNo, HttpServletResponse response) {
		if (userNo == null) {
			throw new IllegalArgumentException("로그인이 필요합니다.");
		}

		String accessToken = jwtTokenProvider.createAccessToken(userNo);
		RefreshTokenIssue refresh = jwtTokenProvider.createRefreshTokenIssue(userNo);

		accessTokenCookieWriter.addCookie(response, accessToken);
		refreshTokenCookieWriter.addCookie(response, refresh.refreshToken());

		refreshTokenStore.save(userNo, refresh.jti());
	}
}

