package com.kh.investSpring.domain.user.vo;

import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class User {
	private int userNo;
    private String userName;
    private String email;
    private String phone;
    private String provider;
    private Date createdAt;
    private String status;
    private Date deleteAt;
    private int auth;
    
}
