// domain/user/controller/UserController.java
package com.kh.investSpring.domain.user.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.investSpring.domain.user.dto.UserMeResponse;
import com.kh.investSpring.domain.user.dto.UserResetPasswordRequest;
import com.kh.investSpring.domain.user.dto.UserSignInRequest;
import com.kh.investSpring.domain.user.dto.UserSignInResponse;
import com.kh.investSpring.domain.user.dto.UserSignUpRequest;
import com.kh.investSpring.domain.user.dto.UserSignUpResponse;
import com.kh.investSpring.domain.user.dto.UserUpdateRequest;
import com.kh.investSpring.domain.user.service.UserService;
import com.kh.investSpring.global.common.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService us;
    
    // 로컬 회원가입
    @PostMapping("/signup")
    public ApiResponse<UserSignUpResponse> signUp(@RequestBody UserSignUpRequest request) {
        UserSignUpResponse response = us.signUp(request);

        return ApiResponse.success(response, "회원가입 성공");
    }
    
    // 로그인 (테스트용)
    @PostMapping("/signin")
    public ApiResponse<UserSignInResponse> login(
            @RequestBody UserSignInRequest request
    ) {
        System.out.println("로그인 컨트롤러 진입");
        System.out.println("userId = " + request.getUserId());

        UserSignInResponse response = us.signIn(request);

        System.out.println("로그인 서비스 처리 완료");

        return ApiResponse.success(response, "로그인 성공");
    }

    // 인증 테스트
    @GetMapping("/me")
    public ApiResponse<?> me(HttpServletRequest request) {
    	Long userNo = (Long) request.getAttribute("userNo");

        UserMeResponse response = us.getMyInfo(userNo);

        return ApiResponse.success(response, "내 정보 조회 성공");
    }
    
    // 탈퇴
    @PatchMapping("/me/withdraw")
    public ApiResponse<?> withdraw(HttpServletRequest request) {
        Long userNo = (Long) request.getAttribute("userNo");

        us.userDelete(userNo);

        return ApiResponse.success(null, "회원 탈퇴가 완료되었습니다.");
    }
    
    @PatchMapping("/me")
    public ApiResponse<UserMeResponse> updateMyInfo(
            HttpServletRequest request,
            @RequestBody UserUpdateRequest updateRequest
    ) {
        Long userNo = (Long) request.getAttribute("userNo");

        UserMeResponse response = us.updateMyInfo(userNo, updateRequest);

        return ApiResponse.success(response, "회원정보 수정 성공");
    }
    
    //비밀번호 변경
    @PostMapping("/find_password")
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