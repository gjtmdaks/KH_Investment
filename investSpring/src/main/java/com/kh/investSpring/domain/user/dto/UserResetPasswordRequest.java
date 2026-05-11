package com.kh.investSpring.domain.user.dto;

import lombok.Getter;

@Getter
public class UserResetPasswordRequest {

    private String userId;
    private String userName;
    private String newPassword;
}