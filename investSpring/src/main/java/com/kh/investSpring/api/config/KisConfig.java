package com.kh.investSpring.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import io.github.cdimascio.dotenv.Dotenv;

@Configuration
public class KisConfig {

    private final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }

    @Bean
    public KisProperties kisProperties() {
        String appKey = getEnv("KIS_APP_KEY");
        String appSecret = getEnv("KIS_APP_SECRET");
        String baseUrl = getEnv("KIS_BASE_URL");

        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://openapi.koreainvestment.com:9443";
        }

        return new KisProperties(appKey, appSecret, baseUrl);
    }

    private String getEnv(String key) {
        String value = System.getenv(key);

        if (value == null || value.isBlank()) {
            value = dotenv.get(key);
        }

        return value;
    }

    public record KisProperties(
            String appKey,
            String appSecret,
            String baseUrl
    ) {
    }
}