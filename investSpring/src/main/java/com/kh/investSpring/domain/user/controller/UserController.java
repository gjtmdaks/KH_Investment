// domain/user/controller/UserController.java
package com.kh.investSpring.domain.user.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.investSpring.domain.user.dto.UserResetPasswordRequest;
import com.kh.investSpring.domain.user.dto.UserSignInRequest;
import com.kh.investSpring.domain.user.dto.UserSignInResponse;
import com.kh.investSpring.domain.user.dto.UserSignUpRequest;
import com.kh.investSpring.domain.user.dto.UserSignUpResponse;
import com.kh.investSpring.domain.user.service.UserService;
import com.kh.investSpring.global.common.ApiResponse;
import com.kh.investSpring.global.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
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
    public ApiResponse<?> login(
    		@RequestBody UserSignInRequest request
    		) {
    	UserSignInResponse response = us.signIn(request);

        return ApiResponse.success(response, "로그인 성공");
    }

    // 인증 테스트
    @GetMapping("/me")
    public ApiResponse<?> me(HttpServletRequest request) {
        Long userNo = (Long) request.getAttribute("userNo");

        return ApiResponse.success(userNo, "내 정보");
    }
    
    @PostMapping("/reset-password")
    public ApiResponse<?> resetPassword(
            @RequestBody UserResetPasswordRequest request
    ) {

        us.resetPassword(request);

        return ApiResponse.success(
                null,
                "비밀번호 변경 성공"
        );
    }
    
}