package com.kh.investSpring.domain.auth.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriComponentsBuilder;

import com.kh.investSpring.domain.auth.dto.LogoutResponse;
import com.kh.investSpring.domain.auth.service.AuthLogoutService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/users")
public class AuthLogoutPageController {

	private final AuthLogoutService authLogoutService;
	private final String frontendMainUri;

	public AuthLogoutPageController(
			AuthLogoutService authLogoutService,
			@Value("${app.oauth2.frontend-main-uri:http://localhost:3000/main}") String frontendMainUri) {
		this.authLogoutService = authLogoutService;
		this.frontendMainUri = frontendMainUri;
	}

	@GetMapping("/logout/kakao")
	public String logoutKakao(HttpServletRequest request, HttpServletResponse response) {
		LogoutResponse logoutResponse = authLogoutService.logout(request, response);
		String kakaoLogoutUrl = logoutResponse.kakaoLogoutUrl();

		if (kakaoLogoutUrl != null && !kakaoLogoutUrl.isBlank()) {
			return "redirect:" + kakaoLogoutUrl;
		}

		return "redirect:" + buildFrontendMainLoggedOutUri();
	}

	private String buildFrontendMainLoggedOutUri() {
		return UriComponentsBuilder.fromUriString(frontendMainUri)
				.queryParam("auth", "logged_out")
				.build()
				.encode()
				.toUriString();
	}
}
