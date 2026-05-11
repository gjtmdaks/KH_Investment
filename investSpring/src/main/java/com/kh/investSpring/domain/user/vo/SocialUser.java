package com.kh.investSpring.domain.user.vo;

import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SocialUser {
	private int socialNo;
    private String providerId;
    private Date createdAt;
    private int userNo;
    
    public SocialUser(String providerId, int userNo) {
        this.providerId = providerId;
        this.userNo = userNo;
    }
}
