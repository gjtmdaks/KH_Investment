package com.kh.investSpring.api.kis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;

@Configuration
public class KisConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }
    
    @Bean
    public StandardWebSocketClient standardWebSocketClient() {

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        container.setDefaultMaxTextMessageBufferSize(1024 * 1024);
        container.setDefaultMaxBinaryMessageBufferSize(1024 * 1024);

        return new StandardWebSocketClient(container);
    }
}