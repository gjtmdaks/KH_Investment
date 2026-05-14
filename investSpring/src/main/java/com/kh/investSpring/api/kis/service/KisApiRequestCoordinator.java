package com.kh.investSpring.api.kis.service;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

import reactor.netty.http.client.PrematureCloseException;

@Component
public class KisApiRequestCoordinator {

    private static final int MAX_RATE_LIMIT_RETRIES = 5;
    private static final long RATE_LIMIT_BACKOFF_MS = 5_000L;
    private static final int MAX_PREMATURE_CLOSE_RETRIES = 3;
    private static final long PREMATURE_CLOSE_BACKOFF_MS = 350L;
    private static final long MIN_REQUEST_INTERVAL_MS = 250L;

    private final Object requestLock = new Object();
    private long lastRequestAtMillis = 0L;

    public <T> T execute(Supplier<T> request) {
        int rateLimitRetry = 0;
        int prematureCloseRetry = 0;

        while (true) {
            waitForRequestSlot();

            try {
                return request.get();
            } catch (HttpStatusCodeException exception) {
                if (!isRateLimitError(exception) || rateLimitRetry >= MAX_RATE_LIMIT_RETRIES) {
                    throw exception;
                }

                rateLimitRetry++;
                sleep(RATE_LIMIT_BACKOFF_MS);
            } catch (ResourceAccessException exception) {
                if (!hasPrematureCloseInCauseChain(exception)
                        || prematureCloseRetry >= MAX_PREMATURE_CLOSE_RETRIES) {
                    throw exception;
                }

                prematureCloseRetry++;
                sleep(PREMATURE_CLOSE_BACKOFF_MS);
            } catch (RuntimeException exception) {
                if (!hasPrematureCloseInCauseChain(exception)
                        || prematureCloseRetry >= MAX_PREMATURE_CLOSE_RETRIES) {
                    throw exception;
                }

                prematureCloseRetry++;
                sleep(PREMATURE_CLOSE_BACKOFF_MS);
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

    private boolean hasPrematureCloseInCauseChain(Throwable throwable) {
        Throwable current = throwable;

        while (current != null) {
            if (current instanceof PrematureCloseException) {
                return true;
            }
            current = current.getCause();
        }

        return false;
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
