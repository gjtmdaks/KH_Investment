package com.kh.investSpring.global.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

	private static final String CLAIM_TOKEN_TYPE = "typ";
	private static final String TYPE_ACCESS = "ACCESS";
	private static final String TYPE_REFRESH = "REFRESH";

	private final long accessExpirationMs;
	private final long refreshExpirationMs;
	private final SecretKey accessKey;
	private final SecretKey refreshKey;

	public JwtTokenProvider(
	        @Value("${jwt.secret}") String accessSecretRaw,
	        @Value("${jwt.refresh-secret}") String refreshSecretRaw,
	        @Value("${jwt.access-expiration-ms:3600000}") long accessExpirationMs,
	        @Value("${jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs) {
	    
	    if (accessSecretRaw == null || accessSecretRaw.isBlank() || refreshSecretRaw == null || refreshSecretRaw.isBlank()) {
	        throw new IllegalArgumentException("JWT secret keys must not be null or empty");
	    }

	    this.accessExpirationMs = accessExpirationMs;
	    this.refreshExpirationMs = refreshExpirationMs;
	    this.accessKey = toSecretKey(accessSecretRaw);
	    this.refreshKey = toSecretKey(refreshSecretRaw);
	}

	private static SecretKey toSecretKey(String raw) {
		try {
			byte[] bytes = Decoders.BASE64.decode(raw);
			if (bytes.length < 32) {
				return Keys.hmacShaKeyFor(raw.getBytes(StandardCharsets.UTF_8));
			}
			return Keys.hmacShaKeyFor(bytes);
		} catch (Exception e) {
			byte[] rawBytes = raw.getBytes(StandardCharsets.UTF_8);
			if (rawBytes.length < 32) {
				throw new IllegalArgumentException("JWT secret key must be at least 32 bytes (256 bits) long");
			}
			return Keys.hmacShaKeyFor(rawBytes);
		}
	}

	public String createAccessToken(Long userNo) {
		return buildToken(userNo, TYPE_ACCESS, accessKey, accessExpirationMs);
	}

	public String createRefreshToken(Long userNo) {
		return buildToken(userNo, TYPE_REFRESH, refreshKey, refreshExpirationMs);
	}

	private String buildToken(Long userNo, String tokenType, SecretKey key, long ttlMs) {
		Date now = new Date();
		return Jwts.builder()
				.setSubject(String.valueOf(userNo))
				.setIssuedAt(now)
				.setExpiration(new Date(now.getTime() + ttlMs))
				.claim(CLAIM_TOKEN_TYPE, tokenType)
				.signWith(key)
				.compact();
	}

	public Long parseAccessTokenUserNo(String token) {
		Claims claims = parseAndValidate(token, accessKey, TYPE_ACCESS);
		return Long.parseLong(claims.getSubject());
	}

	public Long parseRefreshTokenUserNo(String token) {
		Claims claims = parseAndValidate(token, refreshKey, TYPE_REFRESH);
		return Long.parseLong(claims.getSubject());
	}

	private Claims parseAndValidate(String token, SecretKey key, String expectedType) {
		Claims claims = Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token)
				.getBody();
		String typ = (String) claims.get(CLAIM_TOKEN_TYPE);
		if (!expectedType.equals(typ)) {
			throw new IllegalArgumentException("Unexpected token type");
		}
		return claims;
	}
}