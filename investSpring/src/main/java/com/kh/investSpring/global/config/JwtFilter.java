// global/config/JwtFilter.java
package com.kh.investSpring.global.config;

import java.io.IOException;

import com.kh.investSpring.global.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * {@code Bearer} 헤더가 있으면 {@link JwtUtil}로 userNo 파싱 후 request attribute 설정.
 * <p>
 * 무효·만료 토큰은 요청을 막지 않고 그대로 통과합니다(비인증 상태). 과거처럼
 * {@link RuntimeException}을 던지면 메인 등 전역 요청에서 500이 발생합니다.
 * Spring Security JWT 처리는 {@code JwtAuthenticationFilter}가 담당합니다.
 */
public class JwtFilter implements Filter {

	private final JwtUtil jwtUtil;

	public JwtFilter(JwtUtil jwtUtil) {
		this.jwtUtil = jwtUtil;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		res.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
		res.setHeader("Access-Control-Allow-Methods", "*");
		res.setHeader("Access-Control-Allow-Headers", "*");

		if (req.getMethod().equals("OPTIONS")) {
		    res.setStatus(HttpServletResponse.SC_OK);
		    return;
		}

		String authHeader = req.getHeader("Authorization");

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7).trim();
			if (!token.isEmpty()) {
				try {
					Long userNo = jwtUtil.getUserNo(token);
					req.setAttribute("userNo", userNo);
				} catch (Exception ignored) {
					// 만료·위조·형식 오류: attribute 없이 진행 (게스트)
				}
			}
		}

		chain.doFilter(request, response);
	}
}