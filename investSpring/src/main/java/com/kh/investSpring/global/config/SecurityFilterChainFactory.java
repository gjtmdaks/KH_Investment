package com.kh.investSpring.global.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfigurationSource;

import com.kh.investSpring.global.security.JsonAuthenticationEntryPoint;
import com.kh.investSpring.global.security.JwtAuthenticationFilter;

@Component
public class SecurityFilterChainFactory {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint;
	private final AuthenticationSuccessHandler oAuth2LoginSuccessHandler;
	private final OAuth2AuthorizationRequestResolver oauth2AuthorizationRequestResolver;
	public SecurityFilterChainFactory(
			JwtAuthenticationFilter jwtAuthenticationFilter,
			JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint,
			@Qualifier("oauth2LoginSuccessHandler") AuthenticationSuccessHandler oAuth2LoginSuccessHandler,
			OAuth2AuthorizationRequestResolver oauth2AuthorizationRequestResolver) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.jsonAuthenticationEntryPoint = jsonAuthenticationEntryPoint;
		this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
		this.oauth2AuthorizationRequestResolver = oauth2AuthorizationRequestResolver;
	}

	public SecurityFilterChain build(
			HttpSecurity http,
			CorsConfigurationSource corsConfigurationSource,
			boolean requireHttps) throws Exception {
		return build(http, corsConfigurationSource, requireHttps, new String[0]);
	}

	public SecurityFilterChain build(
			HttpSecurity http,
			CorsConfigurationSource corsConfigurationSource,
			boolean requireHttps,
			String... extraPermitAllPathPatterns) throws Exception {

		CookieCsrfTokenRepository csrfRepo = CookieCsrfTokenRepository.withHttpOnlyFalse();
		csrfRepo.setCookiePath("/");

		http.cors(c -> c.configurationSource(corsConfigurationSource));

		if (requireHttps) {
			http.redirectToHttps(Customizer.withDefaults());
		}

		http/* .csrf(AbstractHttpConfigurer::disable) csrf 사이트요청 인증제외가 많아지면 차라리 이걸 쓰도록*/
				.csrf(csrf -> {
					csrf.csrfTokenRepository(csrfRepo)
							.ignoringRequestMatchers(
									"/oauth2/**", 
									"/login/oauth2/**",
									"/logout/oauth2/**",
									"/api/public/**",
									"/api/stocks/**",
									"/stock/**",
									"/users/login",
						            "/users/signin",
						            "/users/signup",
						            "/users/logout",
                                    "/users/me",
						            "/users/logout/**",
						            "/users/find_password",
						            "/users/me/investment-type",
                                    "/users/me/withdraw",
                                    "/watchlist/**",
                                    "/orders/**",
                                    "/recent-view/**",
                                    "/api/board/**",
						            "/admin/**");
					if (extraPermitAllPathPatterns.length > 0) {
						csrf.ignoringRequestMatchers(extraPermitAllPathPatterns);
					}
				})
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.oauth2Login(oauth2 -> oauth2
						.authorizationEndpoint(authorization -> authorization
								.authorizationRequestResolver(oauth2AuthorizationRequestResolver))
						.successHandler(oAuth2LoginSuccessHandler))
				.exceptionHandling(ex -> ex.authenticationEntryPoint(jsonAuthenticationEntryPoint))
				.authorizeHttpRequests(auth -> {
					auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
					auth.requestMatchers(HttpMethod.GET, "/users/me").permitAll();
					auth.requestMatchers(HttpMethod.GET, "/api/board/stocks/**").permitAll();
					auth.requestMatchers(SecurityPathPatterns.AUTH_WHITELIST).permitAll();
					auth.requestMatchers(SecurityPathPatterns.PUBLIC_WHITELIST).permitAll();
					if (extraPermitAllPathPatterns.length > 0) {
						auth.requestMatchers(extraPermitAllPathPatterns).permitAll();
					}
					auth.anyRequest().authenticated();
				})
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
