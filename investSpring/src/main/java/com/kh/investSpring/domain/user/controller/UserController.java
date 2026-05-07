// domain/user/controller/UserController.java
package com.kh.investSpring.domain.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.investSpring.domain.user.dto.UserSignUpRequest;
import com.kh.investSpring.domain.user.dto.UserSignUpResponse;
import com.kh.investSpring.domain.user.service.UserService;
import com.kh.investSpring.global.common.ApiResponse;
import com.kh.investSpring.global.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService us;
    private final JwtUtil jwtUtil;
    
    // 로컬 회원가입
    @PostMapping("/signup")
    public ApiResponse<UserSignUpResponse> signUp(@RequestBody UserSignUpRequest request) {
        UserSignUpResponse response = us.signUp(request);

        return ApiResponse.success(response, "회원가입 성공");
    }
    
    // 로그인 (테스트용)
    @PostMapping("/signin")
    public ApiResponse<?> login() {
        Long userNo = 1L; // 테스트
        String token = jwtUtil.createToken(userNo);

        return ApiResponse.success(token, "로그인 성공");
    }

    // 인증 테스트
    @GetMapping("/me")
    public ApiResponse<?> me(HttpServletRequest request) {
        Long userNo = (Long) request.getAttribute("userNo");

        return ApiResponse.success(userNo, "내 정보");
    }
}