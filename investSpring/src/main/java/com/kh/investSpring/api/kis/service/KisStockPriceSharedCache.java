package com.kh.investSpring.api.kis.service;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.investSpring.api.kis.config.KisProperties;
import com.kh.investSpring.api.kis.dto.KisStockPriceResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisStockPriceSharedCache {

    private static final String KEY_PREFIX = "invest:kis:price:";

    private final KisProperties kisProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public Optional<KisStockPriceResponse> get(String stockCode) {
        if (!kisProperties.isPriceRedisCacheEnabled()) {
            return Optional.empty();
        }

        String key = key(stockCode);
        if (key.isBlank()) {
            return Optional.empty();
        }

        try {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (json == null || json.isBlank()) {
                return Optional.empty();
            }
            KisStockPriceResponse parsed = objectMapper.readValue(json, KisStockPriceResponse.class);
            return Optional.ofNullable(parsed);
        } catch (Exception e) {
            log.debug("KIS price Redis cache read miss stockCode={}", stockCode, e);
            return Optional.empty();
        }
    }

    public void put(String stockCode, KisStockPriceResponse response) {
        if (!kisProperties.isPriceRedisCacheEnabled() || response == null) {
            return;
        }

        String key = key(stockCode);
        if (key.isBlank()) {
            return;
        }

        try {
            Duration ttl = Duration.ofSeconds(Math.max(1L, kisProperties.getPriceRedisCacheTtlSeconds()));
            stringRedisTemplate.opsForValue().set(
                    key,
                    objectMapper.writeValueAsString(response),
                    ttl);
        } catch (JsonProcessingException e) {
            log.debug("KIS price Redis cache write skip stockCode={}", stockCode, e);
        }
    }

    private String key(String stockCode) {
        if (stockCode == null) {
            return "";
        }
        String trimmed = stockCode.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        return KEY_PREFIX + trimmed;
    }
}
