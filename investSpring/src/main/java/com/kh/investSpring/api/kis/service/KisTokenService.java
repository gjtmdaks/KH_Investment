package com.kh.investSpring.api.kis.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kh.investSpring.api.kis.config.KisProperties;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KisTokenService {

    private static final long MIN_TOKEN_ISSUE_INTERVAL_MS = 61_000L;

    private static final int TOKEN_ISSUE_MAX_ATTEMPTS = 2;

    private final RestClient restClient;
    private final KisProperties kisProperties;
    private final KisApiRequestCoordinator kisApiRequestCoordinator;

    private final ReentrantLock tokenIssueSingleFlight = new ReentrantLock();
    private final Object tokenStateLock = new Object();
    private String accessToken;
    private LocalDateTime expiredAt;
    private long lastTokenIssueAttemptMillis;

    private static final DateTimeFormatter KIS_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public KisTokenService(
            RestClient restClient,
            KisProperties kisProperties,
            KisApiRequestCoordinator kisApiRequestCoordinator) {
        this.restClient = restClient;
        this.kisProperties = kisProperties;
        this.kisApiRequestCoordinator = kisApiRequestCoordinator;
    }

    public String getAccessToken() {
        synchronized (tokenStateLock) {
            if (accessToken != null && !isTokenExpiredSoon()) {
                return accessToken;
            }

            if (shouldReuseTokenInsteadOfIssuing()) {
                return accessToken;
            }
        }

        tokenIssueSingleFlight.lock();
        try {
            synchronized (tokenStateLock) {
                if (accessToken != null && !isTokenExpiredSoon()) {
                    return accessToken;
                }

                if (shouldReuseTokenInsteadOfIssuing()) {
                    return accessToken;
                }
            }

            issueAccessTokenWithBackoff();

            synchronized (tokenStateLock) {
                return accessToken;
            }
        } finally {
            tokenIssueSingleFlight.unlock();
        }
    }

    private boolean shouldReuseTokenInsteadOfIssuing() {
        if (accessToken == null || expiredAt == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (!now.isBefore(expiredAt)) {
            long secondsPastExpiry = Duration.between(expiredAt, now).getSeconds();
            if (secondsPastExpiry > 5L) {
                return false;
            }
        }

        long elapsed = System.currentTimeMillis() - lastTokenIssueAttemptMillis;
        if (elapsed >= MIN_TOKEN_ISSUE_INTERVAL_MS) {
            return false;
        }

        if (now.isBefore(expiredAt.minusSeconds(5))) {
            log.warn(
                    "KIS 토큰 발급 간격 제한 구간에서 기존 토큰 재사용(남은 대략 {}초)",
                    Duration.between(now, expiredAt).getSeconds());
            return true;
        }

        return false;
    }

    private boolean isTokenExpiredSoon() {
        if (expiredAt == null) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        if (!now.isBefore(expiredAt)) {
            return true;
        }

        long secondsLeft = Duration.between(now, expiredAt).getSeconds();
        if (secondsLeft <= 0L) {
            return true;
        }

        long leadSeconds = refreshLeadSeconds(secondsLeft);
        return secondsLeft <= leadSeconds;
    }

    private static long refreshLeadSeconds(long secondsUntilExpiry) {
        if (secondsUntilExpiry > 3_600L) {
            return 300L;
        }
        if (secondsUntilExpiry > 600L) {
            return 120L;
        }
        if (secondsUntilExpiry > 120L) {
            return 60L;
        }
        return Math.max(5L, secondsUntilExpiry / 5L);
    }

    private void issueAccessTokenWithBackoff() {
        IllegalStateException lastFailure = null;

        for (int attempt = 0; attempt < TOKEN_ISSUE_MAX_ATTEMPTS; attempt++) {
            synchronized (tokenStateLock) {
                lastTokenIssueAttemptMillis = System.currentTimeMillis();
            }

            try {
                issueAccessTokenOnce();
                return;
            } catch (HttpClientErrorException ex) {
                if (ex.getStatusCode().value() != 403 || !isTokenIssueRateLimited(ex)) {
                    throw ex;
                }

                synchronized (tokenStateLock) {
                    if (accessToken != null && expiredAt != null && LocalDateTime.now().isBefore(expiredAt)) {
                        log.warn("KIS 토큰 발급 한도(EGW00133): 기존 유효 토큰 유지");
                        return;
                    }
                }

                long waitMillis = computeBackoffAfterRateLimit(attempt);
                if (waitMillis <= 0L) {
                    lastFailure = new IllegalStateException(
                            "한국투자증권 토큰 발급 한도(EGW00133). 잠시 후 다시 시도하세요.",
                            ex);
                    continue;
                }

                log.warn(
                        "KIS 토큰 발급 한도(EGW00133) — {}ms 대기 후 재시도({}/{})",
                        waitMillis,
                        attempt + 1,
                        TOKEN_ISSUE_MAX_ATTEMPTS);
                sleepQuietly(waitMillis);
            }
        }

        if (lastFailure != null) {
            throw lastFailure;
        }

        throw new IllegalStateException("한국투자증권 access token 발급 실패");
    }

    private static boolean isTokenIssueRateLimited(HttpClientErrorException ex) {
        String body = ex.getResponseBodyAsString();
        return body != null && body.contains("EGW00133");
    }

    private long computeBackoffAfterRateLimit(int attempt) {
        long sinceLast = System.currentTimeMillis() - lastTokenIssueAttemptMillis;
        long need = MIN_TOKEN_ISSUE_INTERVAL_MS - sinceLast;
        if (need > 0L) {
            return need + 500L;
        }
        return attempt == 0 ? MIN_TOKEN_ISSUE_INTERVAL_MS : 0L;
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("KIS 토큰 발급 대기가 중단되었습니다.", e);
        }
    }

    private void issueAccessTokenOnce() {
        String tokenUrl = kisProperties.getBaseUrl() + "/oauth2/tokenP";
        log.info("KIS access token 발급 요청 url={}", tokenUrl);

        Map<String, String> requestBody = Map.of(
                "grant_type", "client_credentials",
                "appkey", kisProperties.getAppKey(),
                "appsecret", kisProperties.getAppSecret());

        KisTokenResponse response = kisApiRequestCoordinator.execute(
                () -> restClient.post()
                        .uri(tokenUrl)
                        .header("content-type", "application/json; charset=utf-8")
                        .body(requestBody)
                        .retrieve()
                        .body(KisTokenResponse.class));

        if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
            throw new IllegalStateException("한국투자증권 access token 발급 실패");
        }

        LocalDateTime nextExpiredAt;
        if (response.expiresIn() != null && response.expiresIn() > 0) {
            nextExpiredAt = LocalDateTime.now().plusSeconds(response.expiresIn());
        } else if (response.accessTokenTokenExpired() != null) {
            nextExpiredAt = LocalDateTime.parse(
                    response.accessTokenTokenExpired(),
                    KIS_DATE_TIME_FORMAT);
        } else {
            nextExpiredAt = LocalDateTime.now().plusHours(23);
        }

        synchronized (tokenStateLock) {
            this.accessToken = response.accessToken();
            this.expiredAt = nextExpiredAt;
        }

        log.info("KIS access token 발급 완료, 만료: {}", nextExpiredAt);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record KisTokenResponse(
        @JsonProperty("access_token")
        String accessToken,
        @JsonProperty("token_type")
        String tokenType,
        @JsonProperty("expires_in")
        Integer expiresIn,
        @JsonProperty("access_token_token_expired")
        String accessTokenTokenExpired
) {
}
}