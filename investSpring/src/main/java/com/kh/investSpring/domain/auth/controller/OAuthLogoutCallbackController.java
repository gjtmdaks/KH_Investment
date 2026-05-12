package com.kh.investSpring.domain.auth.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class OAuthLogoutCallbackController {

	private final String frontendMainUri;

	public OAuthLogoutCallbackController(
			@Value("${app.oauth2.frontend-main-uri:http://localhost:3000/main}") String frontendMainUri) {
		this.frontendMainUri = frontendMainUri;
	}

	@GetMapping("/logout/oauth2/code/kakao")
	public String kakaoLogoutCallback() {
		String redirectUri = UriComponentsBuilder.fromUriString(frontendMainUri)
				.queryParam("auth", "logged_out")
				.build()
				.encode()
				.toUriString();

		return "redirect:" + redirectUri;
	}
}
