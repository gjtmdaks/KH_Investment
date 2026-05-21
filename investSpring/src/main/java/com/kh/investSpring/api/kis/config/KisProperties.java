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

    public boolean isVirtualTrading() {
        return baseUrl != null && baseUrl.contains("openapivts");
    }
}