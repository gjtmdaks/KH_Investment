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

    @Value("${kis.api.base-url}")
    private String baseUrl;
}