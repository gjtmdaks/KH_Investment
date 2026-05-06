// domain/user/controller/UserController.java
package com.kh.investSpring.domain.user.controller;

import com.kh.investSpring.global.common.ApiResponse;
import com.kh.investSpring.global.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/users")
public class UserController {

    // 로그인 (테스트용)
    @PostMapping("/login")
    public ApiResponse<?> login() {
        Long userNo = 1L; // 테스트
        String token = JwtUtil.createToken(userNo);

        return ApiResponse.success(token, "로그인 성공");
    }

    // 인증 테스트
    @GetMapping("/me")
    public ApiResponse<?> me(HttpServletRequest request) {
        Long userNo = (Long) request.getAttribute("userNo");

        return ApiResponse.success(userNo, "내 정보");
    }
}