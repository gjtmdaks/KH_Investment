// global/config/JwtFilter.java
package com.kh.investSpring.global.config;

import java.io.IOException;

import com.kh.investSpring.global.util.JwtUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

public class JwtFilter implements Filter {

    private JwtUtil jwtUtil;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        String authHeader = req.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                Long userNo = jwtUtil.getUserNo(token);
                req.setAttribute("userNo", userNo);
            } catch (Exception e) {
                throw new RuntimeException("유효하지 않은 토큰");
            }
        }

        chain.doFilter(request, response);
    }
}