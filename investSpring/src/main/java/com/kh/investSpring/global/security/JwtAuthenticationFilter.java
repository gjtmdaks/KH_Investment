package com.kh.investSpring.global.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.kh.investSpring.global.jwt.AccessTokenCookieWriter;
import com.kh.investSpring.global.jwt.JwtTokenProvider;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String bearerToken = null;
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			bearerToken = authHeader.substring(7);
		}

		String tokenFromCookie = readAccessTokenCookie(request);
		String tokenForAuth = bearerToken != null && !bearerToken.isBlank() ? bearerToken : tokenFromCookie;

		if (tokenForAuth != null && !tokenForAuth.isBlank()) {
			try {
				Long userNo = jwtTokenProvider.parseAccessTokenUserNo(tokenForAuth);
				request.setAttribute("userNo", userNo);
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						userNo,
						null,
						List.of(new SimpleGrantedAuthority("ROLE_USER")));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (Exception e) {
				SecurityContextHolder.clearContext();
			}
		}

		filterChain.doFilter(request, response);
	}

	private static String readAccessTokenCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}
		for (Cookie cookie : cookies) {
			if (AccessTokenCookieWriter.ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
				String v = cookie.getValue();
				return v != null && !v.isBlank() ? v : null;
			}
		}
		return null;
	}
}
