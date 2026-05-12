package com.kh.investSpring.api.kis.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.kh.investSpring.api.kis.config.KisProperties;

@Service
public class KisTokenService {

    private final RestClient restClient;
    private final KisProperties kisProperties;
    private final KisApiRequestCoordinator kisApiRequestCoordinator;

    private String accessToken;
    private LocalDateTime expiredAt;

    private static final DateTimeFormatter KIS_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public KisTokenService(
            RestClient restClient,
            KisProperties kisProperties,
            KisApiRequestCoordinator kisApiRequestCoordinator
    ) {
        this.restClient = restClient;
        this.kisProperties = kisProperties;
        this.kisApiRequestCoordinator = kisApiRequestCoordinator;
    }

    public synchronized String getAccessToken() {
        if (accessToken == null || isTokenExpiredSoon()) {
            issueAccessToken();
        }

        return accessToken;
    }

    private boolean isTokenExpiredSoon() {
        if (expiredAt == null) {
            return true;
        }

        // 만료 5분 전이면 새 토큰 발급
        return LocalDateTime.now().isAfter(expiredAt.minusMinutes(5));
    }

    private void issueAccessToken() {
        Map<String, String> requestBody = Map.of(
                "grant_type", "client_credentials",
                "appkey", kisProperties.getAppKey(),
                "appsecret", kisProperties.getAppSecret()
        );

        KisTokenResponse response = kisApiRequestCoordinator.execute(
                () -> restClient.post()
                        .uri(kisProperties.getBaseUrl() + "/oauth2/tokenP")
                        .header("content-type", "application/json; charset=utf-8")
                        .body(requestBody)
                        .retrieve()
                        .body(KisTokenResponse.class)
        );

        if (response == null || response.access_token() == null) {
            throw new IllegalStateException("한국투자증권 access token 발급 실패");
        }

        this.accessToken = response.access_token();

        if (response.access_token_token_expired() != null) {
            this.expiredAt = LocalDateTime.parse(
                    response.access_token_token_expired(),
                    KIS_DATE_TIME_FORMAT
            );
        } else {
            // 혹시 만료 시간이 안 오면 안전하게 23시간 뒤로 설정
            this.expiredAt = LocalDateTime.now().plusHours(23);
        }

        System.out.println("KIS access token 발급 완료");
        System.out.println("KIS token 만료 시간: " + this.expiredAt);
    }

    private record KisTokenResponse(
            String access_token,
            String token_type,
            Integer expires_in,
            String access_token_token_expired
    ) {
    }
}
