package com.kh.investSpring.domain.user.service;

import com.kh.investSpring.domain.main.dto.MainResponse.Header;
import com.kh.investSpring.domain.user.dto.InvestmentTypeSaveRequest;
import com.kh.investSpring.domain.user.dto.UserMeResponse;
import com.kh.investSpring.domain.user.dto.FindPasswordRequest;
import com.kh.investSpring.domain.user.dto.FindUserIdRequest;
import com.kh.investSpring.domain.user.dto.UserSignInRequest;
import com.kh.investSpring.domain.user.dto.UserSignInResponse;
import com.kh.investSpring.domain.user.dto.UserSignUpRequest;
import com.kh.investSpring.domain.user.dto.UserSignUpResponse;
import com.kh.investSpring.domain.user.dto.UserUpdateRequest;
import com.kh.investSpring.domain.user.dto.VerifyCurrentPasswordRequest;
import com.kh.investSpring.domain.user.dto.VerifyCurrentPasswordResponse;

public interface UserService {

    // ✅ 메인 헤더용
	Header getHeader(Long userNo);
	
	UserSignUpResponse signUp(UserSignUpRequest request);

	UserSignInResponse signIn(UserSignInRequest request);

	void userDelete(Long userNo);
	
	UserMeResponse getMyInfo(Long userNo);

	UserMeResponse updateMyInfo(Long userNo, UserUpdateRequest updateRequest);

	VerifyCurrentPasswordResponse verifyCurrentPassword(
			Long userNo,
			VerifyCurrentPasswordRequest request
	);

	void findUserId(FindUserIdRequest request);

	void issueTemporaryPassword(FindPasswordRequest request);

	InvestmentTypeSaveRequest insertInvestmentType(Long userNo, InvestmentTypeSaveRequest saveRequest);

}