// global/util/JwtUtil.java
package com.kh.investSpring.global.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "my-secret-key-my-secret-key-my-secret-key"; // 32자 이상
    private static final long EXPIRATION = 1000 * 60 * 60; // 1시간

    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    public static String createToken(Long userNo) {
        return Jwts.builder()
                .setSubject(String.valueOf(userNo))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(KEY)
                .compact();
    }

    public static Long getUserNo(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }
}