package com.kh.investSpring.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthCookieConfig {

	@Bean
	public RefreshCookieSettings refreshCookieSettings(
			@Value("${app.auth.refresh-cookie-secure:false}") boolean secure,
			@Value("${app.auth.refresh-cookie-same-site:Lax}") String sameSite) {
		return new RefreshCookieSettings(secure, sameSite);
	}
}
