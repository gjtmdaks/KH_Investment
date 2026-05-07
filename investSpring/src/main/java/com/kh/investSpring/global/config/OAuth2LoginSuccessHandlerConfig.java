package com.kh.investSpring.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.kh.investSpring.domain.auth.service.AuthUserService;
import com.kh.investSpring.global.jwt.JwtTokenProvider;
import com.kh.investSpring.global.jwt.RefreshTokenCookieWriter;
import com.kh.investSpring.global.security.OAuth2JwtAuthenticationSuccessHandler;

/**
 * OAuth2 로그인 성공 핸들러만 등록합니다.
 * {@link SecurityConfig}에 두면 SecurityFilterChainFactory와 순환 참조가 발생하므로 이 설정으로 분리합니다.
 */
@Configuration
public class OAuth2LoginSuccessHandlerConfig {

	@Bean("oauth2LoginSuccessHandler")
	public AuthenticationSuccessHandler oauth2LoginSuccessHandler(
			AuthUserService authUserService,
			JwtTokenProvider jwtTokenProvider,
			RefreshTokenCookieWriter refreshTokenCookieWriter,
			@Value("${app.oauth2.frontend-callback-uri:http://localhost:3000/sign-in/oauth-callback}") String oauth2FrontendCallbackUri) {
		return new OAuth2JwtAuthenticationSuccessHandler(
				authUserService, jwtTokenProvider, refreshTokenCookieWriter, oauth2FrontendCallbackUri);
	}
}
