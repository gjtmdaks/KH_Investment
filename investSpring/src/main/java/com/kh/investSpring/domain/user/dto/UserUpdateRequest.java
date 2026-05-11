package com.kh.investSpring.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateRequest {

    private String userName;
    private String email;
    private String phone;
}
