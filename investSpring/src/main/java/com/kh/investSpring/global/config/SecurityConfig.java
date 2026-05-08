package com.kh.investSpring.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final SecurityFilterChainFactory securityFilterChainFactory;

	@Bean
	public CorsConfigurationSource corsConfigurationSource(
			@Value("${app.cors.allowed-origins:http://localhost:3000}") String allowedOrigins) {
		return CorsConfigurationFactory.createSource(allowedOrigins);
	}

	@Bean
	public SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			CorsConfigurationSource corsConfigurationSource,
			@Value("${app.security.redirect-to-https:false}") boolean redirectToHttps,
			@Value("${app.security.permit-actuator:true}") boolean permitActuator) throws Exception {
		if (permitActuator) {
			return securityFilterChainFactory.build(http, corsConfigurationSource, redirectToHttps, "/actuator/**");
		}
		return securityFilterChainFactory.build(http, corsConfigurationSource, redirectToHttps);
	}
	
	// 비번 암호화 도구
	@Bean
	public PasswordEncoder passwordEncoder() {
	    return new BCryptPasswordEncoder();
	}
}
