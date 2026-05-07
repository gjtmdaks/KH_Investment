package com.kh.investSpring.api.kis.service;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.kh.investSpring.api.kis.config.KisProperties;

@Service
public class KisApprovalService {

    private final RestClient restClient;
    private final KisProperties properties;

    public KisApprovalService(
            RestClient restClient,
            KisProperties properties
    ) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public String getApprovalKey() {

        Map<String, String> body = Map.of(
                "grant_type", "client_credentials",
                "appkey", properties.getAppKey(),
                "secretkey", properties.getAppSecret()
        );

        Map response = restClient.post()
                .uri("https://openapi.koreainvestment.com:9443/oauth2/Approval")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("approval_key") == null) {
            throw new RuntimeException("approval_key 발급 실패");
        }

        return response.get("approval_key").toString();
    }
}