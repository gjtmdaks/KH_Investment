package com.kh.investSpring.global.security;

import java.io.IOException;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import com.kh.investSpring.domain.auth.service.AuthTokenIssueService;
import com.kh.investSpring.domain.auth.service.AuthUserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 카카오 OAuth2 Authorization Code는 Spring Security 클라이언트가 서버에서만 교환합니다.
 * 성공 시 앱 JWT(Access)와 Refresh를 HttpOnly·Secure·SameSite 쿠키로 설정하고,
 * 프론트 콜백으로는 파라미터 없이 리다이렉트(URL에 토큰 미포함, OAuth BCP 권장).
 */
public class OAuth2JwtAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final AuthUserService authUserService;
	private final AuthTokenIssueService authTokenIssueService;
	private final String oauth2FrontendCallbackUri;

	public OAuth2JwtAuthenticationSuccessHandler(
			AuthUserService authUserService,
			AuthTokenIssueService authTokenIssueService,
			String oauth2FrontendCallbackUri) {
		this.authUserService = authUserService;
		this.authTokenIssueService = authTokenIssueService;
		this.oauth2FrontendCallbackUri = oauth2FrontendCallbackUri;
		setAlwaysUseDefaultTargetUrl(true);
	}

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
		
		// 카카오 회원 정보를 DB에 반영하거나, 기존 회원을 조회하여 고유 userNo 획득
		String nickname = resolveNickname(oauth2User.getAttributes());

		Long userNo = authUserService.resolveOrCreateKakaoUser(providerId, nickname);
		
		authTokenIssueService.issue(userNo, response);
		
		// 토큰 파라미터가 없는 리다이렉트 URL 생성
		String redirectUrl = UriComponentsBuilder.fromUriString(oauth2FrontendCallbackUri)
				.build()
				.encode()
				.toUriString();

		setDefaultTargetUrl(redirectUrl);
		super.onAuthenticationSuccess(request, response, authentication);
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
