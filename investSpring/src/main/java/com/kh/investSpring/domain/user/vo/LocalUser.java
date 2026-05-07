package com.kh.investSpring.domain.user.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LocalUser {
	private int userNo;
    private String userId;
    private String password;

    public LocalUser(int userNo, String userId, String password) {
        this.userNo = userNo;
        this.userId = userId;
        this.password = password;
    }
}
