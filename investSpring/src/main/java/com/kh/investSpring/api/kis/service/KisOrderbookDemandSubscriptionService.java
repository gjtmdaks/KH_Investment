package com.kh.investSpring.api.kis.service;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import com.kh.investSpring.api.kis.config.KisProperties;
import com.kh.investSpring.api.kis.websocket.KisOrderbookSubscriptionMessenger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KisOrderbookDemandSubscriptionService {

    private final KisProperties kisProperties;
    private final KisOrderbookSubscriptionMessenger subscriptionMessenger;

    private final Object sessionLock = new Object();

    private volatile WebSocketSession activeSession;
    private volatile String activeApprovalKey;
    private volatile String activeStockCode;

    public void onSessionConnected(WebSocketSession session, String approvalKey) {
        synchronized (sessionLock) {
            this.activeSession = session;
            this.activeApprovalKey = approvalKey;
            resubscribeActiveLocked();
        }
    }

    public void onSessionClosed(WebSocketSession session) {
        synchronized (sessionLock) {
            if (activeSession != null && activeSession.getId().equals(session.getId())) {
                activeSession = null;
                activeApprovalKey = null;
            }
        }
    }

    public boolean subscribeDemand(String stockCode) {
        if (!kisProperties.isWebsocketEnabled()) {
            return false;
        }

        String code = normalizeCode(stockCode);
        if (code.isBlank()) {
            return false;
        }

        synchronized (sessionLock) {
            if (code.equals(activeStockCode)) {
                return isSessionReadyLocked();
            }

            unsubscribeActiveLocked();

            activeStockCode = code;
            if (!isSessionReadyLocked()) {
                log.debug("호가 WS 세션 미연결 — 구독 대기 stockCode={}", code);
                return false;
            }

            try {
                subscriptionMessenger.subscribe(activeSession, activeApprovalKey, code);
                sleepInterval();
                log.info("호가 on-demand 구독 완료 stockCode={}", code);
                return true;
            } catch (Exception e) {
                log.error("호가 on-demand 구독 실패 stockCode={}", code, e);
                activeStockCode = null;
                return false;
            }
        }
    }

    public void unsubscribeDemand(String stockCode) {
        String code = normalizeCode(stockCode);
        synchronized (sessionLock) {
            if (activeStockCode == null || !activeStockCode.equals(code)) {
                return;
            }
            unsubscribeActiveLocked();
            activeStockCode = null;
        }
    }

    public boolean isDemandSubscribed(String stockCode) {
        if (!kisProperties.isWebsocketEnabled()) {
            return false;
        }

        String code = normalizeCode(stockCode);
        synchronized (sessionLock) {
            return isSessionReadyLocked() && code.equals(activeStockCode);
        }
    }

    private void resubscribeActiveLocked() {
        if (activeStockCode == null || activeStockCode.isBlank()) {
            return;
        }
        if (!isSessionReadyLocked()) {
            return;
        }

        try {
            subscriptionMessenger.subscribe(activeSession, activeApprovalKey, activeStockCode);
            sleepInterval();
            log.info("호가 WS 재연결 후 구독 복구 stockCode={}", activeStockCode);
        } catch (Exception e) {
            log.error("호가 WS 재구독 실패 stockCode={}", activeStockCode, e);
        }
    }

    private void unsubscribeActiveLocked() {
        if (activeStockCode == null || activeStockCode.isBlank() || !isSessionReadyLocked()) {
            return;
        }

        try {
            subscriptionMessenger.unsubscribe(activeSession, activeApprovalKey, activeStockCode);
            sleepInterval();
        } catch (Exception e) {
            log.error("호가 on-demand 구독 해제 실패 stockCode={}", activeStockCode, e);
        }
    }

    private boolean isSessionReadyLocked() {
        return activeSession != null
                && activeSession.isOpen()
                && activeApprovalKey != null
                && !activeApprovalKey.isBlank();
    }

    private void sleepInterval() {
        long intervalMs = Math.max(0L, kisProperties.getWebsocketSubscribeIntervalMs());
        if (intervalMs <= 0L) {
            return;
        }
        try {
            Thread.sleep(intervalMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String normalizeCode(String stockCode) {
        return stockCode == null ? "" : stockCode.trim();
    }
}
