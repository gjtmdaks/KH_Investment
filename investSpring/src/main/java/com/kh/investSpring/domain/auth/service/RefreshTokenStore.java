package com.kh.investSpring.domain.auth.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenStore {

	private static final String KEY_PREFIX = "auth:refresh:";

	private final StringRedisTemplate redis;

	@Value("${jwt.refresh-expiration-ms:604800000}")
	private long refreshExpirationMs;

	public void save(Long userNo, String jti) {
		if (userNo == null || !StringUtils.hasText(jti)) {
			throw new IllegalArgumentException("Refresh 저장 정보가 유효하지 않습니다.");
		}
		String key = KEY_PREFIX + userNo;
		Duration ttl = Duration.ofMillis(Math.max(1L, refreshExpirationMs));
		redis.opsForValue().set(key, jti, ttl);
	}

	public String getJti(Long userNo) {
		if (userNo == null) {
			return null;
		}
		return redis.opsForValue().get(KEY_PREFIX + userNo);
	}

	public void revoke(Long userNo) {
		if (userNo == null) {
			return;
		}
		redis.delete(KEY_PREFIX + userNo);
	}
}

