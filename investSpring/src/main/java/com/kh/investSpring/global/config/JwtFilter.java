// global/config/JwtFilter.java
package com.kh.investSpring.global.config;

import com.kh.investSpring.global.util.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public class JwtFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        String authHeader = req.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                Long userNo = JwtUtil.getUserNo(token);
                req.setAttribute("userNo", userNo);
            } catch (Exception e) {
                throw new RuntimeException("유효하지 않은 토큰");
            }
        }

        chain.doFilter(request, response);
    }
}