package com.kh.investSpring.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResetPasswordRequest {

    private String userId;
    private String userName;
    private String newPassword;
}
