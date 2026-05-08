package com.kh.investSpring.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserSignInRequest {
	private String userId;
    private String password;
}
