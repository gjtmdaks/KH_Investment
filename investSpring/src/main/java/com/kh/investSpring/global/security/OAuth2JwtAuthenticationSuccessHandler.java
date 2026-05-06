package com.kh.investSpring.global.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.investSpring.domain.auth.dto.LoginResponse;
import com.kh.investSpring.domain.auth.service.AuthUserService;
import com.kh.investSpring.global.common.ApiResponse;
import com.kh.investSpring.global.jwt.JwtTokenProvider;
import com.kh.investSpring.global.jwt.RefreshTokenCookieWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * OAuth2 Authorization Code Grant 성공 시 서비스용 JWT(Access)를 발급하고,
 * Refresh는 HttpOnly 쿠키로 내려준 뒤 JSON 본문으로 Access Token을 반환합니다.
 */
@Component("oauth2LoginSuccessHandler")
@RequiredArgsConstructor
public class OAuth2JwtAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private final AuthUserService authUserService;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenCookieWriter refreshTokenCookieWriter;
	private final ObjectMapper objectMapper;

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		if (response.isCommitted()) {
			return;
		}

		Object principal = authentication.getPrincipal();
		if (!(principal instanceof OAuth2User oauth2User)) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAuth2 principal 오류");
			return;
		}

		String providerId = oauth2User.getName();
		String nickname = resolveNickname(oauth2User.getAttributes());

		Long userNo = authUserService.resolveOrCreateKakaoUser(providerId, nickname);

		String accessToken = jwtTokenProvider.createAccessToken(userNo);
		String refreshToken = jwtTokenProvider.createRefreshToken(userNo);
		refreshTokenCookieWriter.addCookie(response, refreshToken);

		ApiResponse<LoginResponse> body = ApiResponse.success(new LoginResponse(accessToken), "카카오 로그인 성공");

		response.setStatus(HttpServletResponse.SC_OK);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write(objectMapper.writeValueAsString(body));
	}

	private static String resolveNickname(Map<String, Object> attributes) {
		Object id = attributes.get("id");
		Object acc = attributes.get("kakao_account");
		if (acc instanceof Map<?, ?> accountMap) {
			Object profile = accountMap.get("profile");
			if (profile instanceof Map<?, ?> profileMap) {
				Object nick = profileMap.get("nickname");
				if (nick != null) {
					return nick.toString();
				}
			}
		}
		return id != null ? "User_" + id : "User_unknown";
	}
}
