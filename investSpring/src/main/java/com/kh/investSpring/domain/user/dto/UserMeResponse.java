package com.kh.investSpring.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserMeResponse {

    private int userNo;
    private String userId;
    private String userName;
    private String email;
    private String phone;
    private String provider;
    private Integer auth;
    
    private Integer investmentTotalPoint;
    private String investmentType;
}