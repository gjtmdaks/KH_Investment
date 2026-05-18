// domain/user/controller/UserController.java
package com.kh.investSpring.domain.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.investSpring.domain.auth.dto.LogoutResponse;
import com.kh.investSpring.domain.auth.service.AuthLogoutService;
import com.kh.investSpring.domain.user.dto.InvestmentTypeSaveRequest;
import com.kh.investSpring.domain.user.dto.UserMeResponse;
import com.kh.investSpring.domain.user.dto.UserResetPasswordRequest;
import com.kh.investSpring.domain.user.dto.UserSignInRequest;
import com.kh.investSpring.domain.user.dto.UserSignInResponse;
import com.kh.investSpring.domain.user.dto.EmailSendCodeRequest;
import com.kh.investSpring.domain.user.dto.EmailVerifyCodeRequest;
import com.kh.investSpring.domain.user.dto.UserSignUpRequest;
import com.kh.investSpring.domain.user.dto.UserSignUpResponse;
import com.kh.investSpring.domain.user.dto.UserUpdateRequest;
import com.kh.investSpring.domain.user.service.SignupEmailVerificationService;
import com.kh.investSpring.domain.user.service.UserService;
import com.kh.investSpring.global.common.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService us;
	private final AuthLogoutService authLogoutService;
	private final SignupEmailVerificationService signupEmailVerificationService;

    @PostMapping("/signup/email/send-code")
    public ApiResponse<Void> sendSignupEmailCode(@RequestBody EmailSendCodeRequest request) {
        signupEmailVerificationService.sendVerificationCode(request.email());
        return ApiResponse.success(null, "인증번호를 이메일로 발송했습니다.");
    }

    @PostMapping("/signup/email/verify-code")
    public ApiResponse<Void> verifySignupEmailCode(@RequestBody EmailVerifyCodeRequest request) {
        signupEmailVerificationService.verifyCode(request.email(), request.code());
        return ApiResponse.success(null, "이메일 인증이 완료되었습니다.");
    }

    // 로컬 회원가입
    @PostMapping("/signup")
    public ApiResponse<UserSignUpResponse> signUp(@RequestBody UserSignUpRequest request) {
        UserSignUpResponse response = us.signUp(request);

        return ApiResponse.success(response, "회원가입 성공");
    }
    
    // 로그인
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
    
    @PostMapping("/logout")
    public ApiResponse<LogoutResponse> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        LogoutResponse logoutResponse = authLogoutService.logout(request, response);

        return ApiResponse.success(logoutResponse, "로그아웃 성공");
    }

    // 내 정보
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
    
    // 투자성향 분석 결과 저장
    @PostMapping("/me/investment-type")
    public ApiResponse<InvestmentTypeSaveRequest> saveInvestmentType(
            HttpServletRequest request,
            @RequestBody InvestmentTypeSaveRequest saveRequest
    ) {
        Long userNo = (Long) request.getAttribute("userNo");

        InvestmentTypeSaveRequest response =
                us.insertInvestmentType(userNo, saveRequest);

        return ApiResponse.success(response, "투자성향 분석 결과 저장 성공");
    }
    
}