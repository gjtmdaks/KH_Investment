package com.kh.investSpring.global.config;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

public final class CorsConfigurationFactory {

	private CorsConfigurationFactory() {
	}

	public static CorsConfigurationSource createSource(String commaSeparatedOrigins) {
		List<String> origins = resolveAllowedOrigins(commaSeparatedOrigins);

		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(origins);
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	public static List<String> resolveAllowedOrigins(String commaSeparatedOrigins) {
		Set<String> origins = new LinkedHashSet<>();

		if (commaSeparatedOrigins != null) {
			Arrays.stream(commaSeparatedOrigins.split(","))
					.map(CorsConfigurationFactory::normalizeOrigin)
					.filter(s -> !s.isEmpty())
					.forEach(origins::add);
		}

		origins.add(TeamDevHost.FRONTEND_ORIGIN);
		origins.add("http://localhost:" + TeamDevHost.FRONTEND_PORT);
		origins.add("http://127.0.0.1:" + TeamDevHost.FRONTEND_PORT);

		return List.copyOf(origins);
	}

	public static String[] resolveAllowedOriginArray(String commaSeparatedOrigins) {
		List<String> origins = resolveAllowedOrigins(commaSeparatedOrigins);
		return origins.toArray(String[]::new);
	}

	private static String normalizeOrigin(String origin) {
		String normalized = origin == null ? "" : origin.trim();
		while (normalized.endsWith("/")) {
			normalized = normalized.substring(0, normalized.length() - 1);
		}
		return normalized;
	}
}
