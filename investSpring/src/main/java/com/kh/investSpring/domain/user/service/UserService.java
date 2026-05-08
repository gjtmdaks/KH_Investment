package com.kh.investSpring.domain.user.service;

import com.kh.investSpring.domain.main.dto.MainResponse.Header;
import com.kh.investSpring.domain.user.dto.UserSignInRequest;
import com.kh.investSpring.domain.user.dto.UserSignInResponse;
import com.kh.investSpring.domain.user.dto.UserSignUpRequest;
import com.kh.investSpring.domain.user.dto.UserSignUpResponse;

public interface UserService {

    // ✅ 메인 헤더용
	Header getHeader(Long userNo);
	
	UserSignUpResponse signUp(UserSignUpRequest request);

	UserSignInResponse signIn(UserSignInRequest request);
}