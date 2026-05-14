package com.kh.investSpring.api.kis.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
public class KisConfig {

    private static final Duration KIS_MAX_IDLE_TIME = Duration.ofSeconds(35);
    private static final Duration KIS_EVICT_IN_BACKGROUND_INTERVAL = Duration.ofSeconds(20);

    @Bean
    public RestClient restClient() {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("kis")
                .maxIdleTime(KIS_MAX_IDLE_TIME)
                .evictInBackground(KIS_EVICT_IN_BACKGROUND_INTERVAL)
                .build();

        HttpClient nettyHttpClient = HttpClient.create(connectionProvider);

        ReactorClientHttpRequestFactory requestFactory = new ReactorClientHttpRequestFactory(
                nettyHttpClient);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }
    
    @Bean
    public StandardWebSocketClient standardWebSocketClient() {

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        container.setDefaultMaxTextMessageBufferSize(1024 * 1024);
        container.setDefaultMaxBinaryMessageBufferSize(1024 * 1024);

        return new StandardWebSocketClient(container);
    }
}