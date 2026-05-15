package com.kh.investSpring.api.kis.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.investSpring.api.kis.config.KisProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisMinuteChartApiClient {

    private final KisProperties properties;
    private final KisTokenService kisTokenService;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public List<Map<String, Object>> fetch(
            String url,
            String trId,
            String stockCode,
            String logLabel
    ) throws IOException, InterruptedException {
        int retry = 0;

        while (true) {
            String token = kisTokenService.getAccessToken();
            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("content-type", "application/json; charset=utf-8")
                            .header("authorization", "Bearer " + token)
                            .header("appkey", properties.getAppKey())
                            .header("appsecret", properties.getAppSecret())
                            .header("tr_id", trId)
                            .GET()
                            .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );
            String bodyText = response.body();

            if (response.statusCode() != 200) {
                retry++;
                log.warn(
                        "{} HTTP 실패 stockCode={} status={} retry={}",
                        logLabel,
                        stockCode,
                        response.statusCode(),
                        retry
                );

                Thread.sleep(10_000L);

                continue;
            }

            if (bodyText.contains("EGW00201")) {
                retry++;
                long wait = Math.min(60_000L, retry * 10_000L);

                log.warn(
                        "{} rate limit stockCode={} retry={} wait={}ms",
                        logLabel,
                        stockCode,
                        retry,
                        wait
                );

                Thread.sleep(wait);

                continue;
            }

            Map<String, Object> body = objectMapper.readValue(
                    bodyText,
                    new TypeReference<Map<String, Object>>() {}
            );
            String rtCd = String.valueOf(body.get("rt_cd"));

            if (!"0".equals(rtCd)) {
                retry++;

                log.warn(
                        "{} KIS 실패 stockCode={} rt_cd={} msg={} retry={}",
                        logLabel,
                        stockCode,
                        body.get("rt_cd"),
                        body.get("msg1"),
                        retry
                );

                Thread.sleep(10_000L);

                continue;
            }

            Object out2 = KisMinuteBarMapper.extractOutput2(body);

            return KisMinuteBarMapper.castOutputRows(out2);
        }
    }
}
