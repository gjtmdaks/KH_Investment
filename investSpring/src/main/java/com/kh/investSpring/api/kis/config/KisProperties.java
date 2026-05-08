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
    @Value("${kis.websocket.enabled:false}")
    private boolean websocketEnabled;

    @Value("${kis.api.base-url}")
    private String baseUrl;
}