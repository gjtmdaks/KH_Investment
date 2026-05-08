package com.kh.investSpring.domain.main.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.investSpring.domain.main.dto.MainResponse;
import com.kh.investSpring.domain.main.service.MainService;
import com.kh.investSpring.global.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
public class MainController {

    private final MainService mainService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public MainResponse getMain(HttpServletRequest request) {

        String token = extractToken(request);

        // 비로그인 상태
        if (token == null) {
            return mainService.getMain(null);
        }

        // 무효·만료 JWT는 게스트처럼 응답 (프런트 localStorage 등에 남은 토큰 대응)
        try {
            Long userNo = jwtUtil.getUserNo(token);
            return mainService.getMain(userNo);
        } catch (Exception e) {
            return mainService.getMain(null);
        }
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");

        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        return null;
    }
}