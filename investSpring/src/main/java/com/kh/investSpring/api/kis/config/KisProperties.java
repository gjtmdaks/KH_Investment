package com.kh.investSpring.api.kis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Getter
@Configuration
public class KisProperties {

    @Value("${kis.api.appkey}")
    private String appKey;

    @Value("${kis.api.appsecret}")
    private String appSecret;

    @Value("${kis.websocket.url}")
    private String websocketUrl;

    /** 실시간 체결가 WebSocket 연결 여부. 기본 false (다중 로컬 기동 시 KIS 세션 중첩 방지). 켤 때만 true */
    @Value("${kis.websocket.enabled:true}")
    private boolean websocketEnabled;

    @Value("${kis.api.base-url}")
    private String baseUrl;

    /** WS tick → DB 반영 후 이 TTL(ms) 이내면 getStockPrice가 KIS REST 대신 DB 사용 */
    @Value("${kis.price.db-fresh-ttl-ms:5000}")
    private long priceDbFreshTtlMs;

    /** 종목별 시세 Redis 공유 캐시 TTL(초). 다중 사용자 상세 폴링 fan-in */
    @Value("${kis.price.redis-cache-ttl-seconds:3}")
    private long priceRedisCacheTtlSeconds;

    @Value("${kis.price.redis-cache-enabled:true}")
    private boolean priceRedisCacheEnabled;

    /** JVM 로컬 1차 캐시 TTL(ms) */
    @Value("${kis.price.local-cache-ttl-ms:800}")
    private long priceLocalCacheTtlMs;

    /** H0STCNT0 최대 구독 종목 수 (KIS 세션당 41 이하 권장) */
    @Value("${kis.websocket.max-subscriptions:40}")
    private int websocketMaxSubscriptions;

    /** 수요 기반 슬롯(최근 조회·관심) — 나머지는 거래대금 Top */
    @Value("${kis.websocket.demand-slots:8}")
    private int websocketDemandSlots;

    /** recent_view 집계 윈도우(분) */
    @Value("${kis.websocket.demand-recent-minutes:5}")
    private int websocketDemandRecentMinutes;

    /** 구독 풀 재배치 주기(ms). 장중 주기적 Top40 갱신 */
    @Value("${kis.websocket.rebalance-delay-ms:180000}")
    private long websocketRebalanceDelayMs;

    /** 구독/해제 메시지 간격(ms) */
    @Value("${kis.websocket.subscribe-interval-ms:50}")
    private long websocketSubscribeIntervalMs;

    /** 메인 순위·표시용 구간 거래대금 윈도우(분). 60 = 최근 1시간 */
    @Value("${kis.main.trading-window-minutes:60}")
    private int mainTradingWindowMinutes;

    /** 누적 거래대금 스냅샷 저장 주기(ms). 기본 5분 */
    @Value("${kis.main.trading-snapshot-interval-ms:300000}")
    private long mainTradingSnapshotIntervalMs;

    public boolean isVirtualTrading() {
        return baseUrl != null && baseUrl.contains("openapivts");
    }
}