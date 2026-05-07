package com.kh.investSpring.domain.user.dto;

public record UserSignUpRequest(
		String userId,
        String password,
        String userName) {

}
