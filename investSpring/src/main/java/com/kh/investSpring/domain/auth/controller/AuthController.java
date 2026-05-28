package com.kh.investSpring.domain.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.investSpring.domain.auth.service.AuthRefreshService;
import com.kh.investSpring.global.common.ApiResponse;
import com.kh.investSpring.global.common.AuthErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private static final String REFRESH_INVALID_CODE = "REFRESH_INVALID";

	private final AuthRefreshService authRefreshService;

	@PostMapping("/refresh")
	public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
		try {
			authRefreshService.refresh(request, response);
			return ResponseEntity.ok(ApiResponse.success(null, "토큰 갱신 성공"));
		} catch (Exception e) {
			AuthErrorResponse body = new AuthErrorResponse(REFRESH_INVALID_CODE, "다시 로그인해 주세요.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
		}
	}
}

