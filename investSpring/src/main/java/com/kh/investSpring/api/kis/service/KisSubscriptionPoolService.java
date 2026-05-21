package com.kh.investSpring.api.kis.service;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import com.kh.investSpring.api.kis.config.KisProperties;
import com.kh.investSpring.api.kis.websocket.KisRealtimeSubscriptionMessenger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KisSubscriptionPoolService {

    private final KisProperties kisProperties;
    private final KisSubscriptionTargetSelector targetSelector;
    private final KisRealtimeSubscriptionMessenger subscriptionMessenger;

    private final Object rebalanceLock = new Object();
    private final Set<String> subscribedCodes = ConcurrentHashMap.newKeySet();

    private volatile WebSocketSession activeSession;
    private volatile String activeApprovalKey;

    public void onSessionConnected(WebSocketSession session, String approvalKey) {
        synchronized (rebalanceLock) {
            this.activeSession = session;
            this.activeApprovalKey = approvalKey;
            subscribedCodes.clear();
            applyTargetToSession(selectTargetOrFallback());
        }
    }

    public void onSessionClosed(WebSocketSession session) {
        synchronized (rebalanceLock) {
            if (activeSession != null && activeSession.getId().equals(session.getId())) {
                activeSession = null;
                activeApprovalKey = null;
                subscribedCodes.clear();
            }
        }
    }

    public void rebalance() {
        if (!kisProperties.isWebsocketEnabled()) {
            return;
        }

        synchronized (rebalanceLock) {
            WebSocketSession session = activeSession;
            String approvalKey = activeApprovalKey;

            if (session == null || !session.isOpen() || approvalKey == null || approvalKey.isBlank()) {
                log.debug("KIS WS 재배치 스킵 — 활성 세션 없음");
                return;
            }

            LinkedHashSet<String> target = selectTargetOrFallback();
            Set<String> current = new HashSet<>(subscribedCodes);
            Set<String> toRemove = new HashSet<>(current);
            toRemove.removeAll(target);

            Set<String> toAdd = new HashSet<>(target);
            toAdd.removeAll(current);

            if (toRemove.isEmpty() && toAdd.isEmpty()) {
                log.debug("KIS WS 재배치 — 변경 없음 size={}", subscribedCodes.size());
                return;
            }

            log.info("KIS WS 재배치 시작 remove={}, add={}, total={}",
                    toRemove.size(), toAdd.size(), target.size());

            applyDiff(session, approvalKey, toRemove, toAdd);
            subscribedCodes.clear();
            subscribedCodes.addAll(target);
        }
    }

    public Set<String> getSubscribedCodes() {
        return Set.copyOf(subscribedCodes);
    }

    private LinkedHashSet<String> selectTargetOrFallback() {
        LinkedHashSet<String> target = targetSelector.selectTargetStockCodes();
        if (target.isEmpty()) {
            log.warn("KIS WS 구독 대상이 비어 있어 재시도합니다.");
            target = targetSelector.selectTargetStockCodes();
        }
        return target;
    }

    private void applyTargetToSession(LinkedHashSet<String> target) {
        WebSocketSession session = activeSession;
        String approvalKey = activeApprovalKey;
        if (session == null || !session.isOpen() || approvalKey == null) {
            return;
        }

        Set<String> toAdd = new HashSet<>(target);
        applyDiff(session, approvalKey, Set.of(), toAdd);
        subscribedCodes.clear();
        subscribedCodes.addAll(target);
        log.info("KIS WS 초기 구독 완료 size={}", subscribedCodes.size());
    }

    private void applyDiff(
            WebSocketSession session,
            String approvalKey,
            Set<String> toRemove,
            Set<String> toAdd
    ) {
        long intervalMs = Math.max(0L, kisProperties.getWebsocketSubscribeIntervalMs());

        for (String stockCode : toRemove) {
            try {
                subscriptionMessenger.unsubscribe(session, approvalKey, stockCode);
                sleepInterval(intervalMs);
            } catch (Exception e) {
                log.error("KIS 구독 해제 실패 stockCode={}", stockCode, e);
            }
        }

        for (String stockCode : toAdd) {
            try {
                subscriptionMessenger.subscribe(session, approvalKey, stockCode);
                sleepInterval(intervalMs);
            } catch (Exception e) {
                log.error("KIS 구독 실패 stockCode={}", stockCode, e);
            }
        }
    }

    private static void sleepInterval(long intervalMs) {
        if (intervalMs <= 0L) {
            return;
        }
        try {
            Thread.sleep(intervalMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
