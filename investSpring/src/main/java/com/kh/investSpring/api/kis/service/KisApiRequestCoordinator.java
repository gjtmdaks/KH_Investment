package com.kh.investSpring.api.kis.service;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

@Component
public class KisApiRequestCoordinator {

    private static final int MAX_RATE_LIMIT_RETRIES = 5;
    private static final long RATE_LIMIT_BACKOFF_MS = 5_000L;
    private static final long MIN_REQUEST_INTERVAL_MS = 250L;

    private final Object requestLock = new Object();
    private long lastRequestAtMillis = 0L;

    public <T> T execute(Supplier<T> request) {
        int retry = 0;

        while (true) {
            waitForRequestSlot();

            try {
                return request.get();
            } catch (HttpStatusCodeException exception) {
                if (!isRateLimitError(exception) || retry >= MAX_RATE_LIMIT_RETRIES) {
                    throw exception;
                }

                retry++;
                sleep(RATE_LIMIT_BACKOFF_MS);
            }
        }
    }

    private void waitForRequestSlot() {
        synchronized (requestLock) {
            long waitMillis = MIN_REQUEST_INTERVAL_MS
                    - (System.currentTimeMillis() - lastRequestAtMillis);

            if (waitMillis > 0L) {
                sleep(waitMillis);
            }

            lastRequestAtMillis = System.currentTimeMillis();
        }
    }

    private boolean isRateLimitError(HttpStatusCodeException exception) {
        String responseBody = exception.getResponseBodyAsString();

        return responseBody != null
                && (responseBody.contains("EGW00201")
                || responseBody.contains("초당 거래건수를 초과하였습니다"));
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("KIS API 호출 대기가 중단되었습니다.", interruptedException);
        }
    }
}
