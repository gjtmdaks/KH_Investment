package com.kh.investSpring.domain.admin.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserResponse {

    private Long userNo;

    private String userName;

    private String email;

    private String phone;

    private String provider;

    private LocalDateTime createdAt;

    private String status;

    private LocalDateTime deleteAt;

    private Integer auth;

    public String getStatusName() {
        if ("ACTIVE".equals(status)) {
            return "정상";
        }

        if ("STOP".equals(status)) {
            return "정지";
        }

        if ("DELETE".equals(status)) {
            return "탈퇴";
        }

        return "알 수 없음";
    }

    public String getAuthName() {
        if (auth != null && auth == 1) {
            return "관리자";
        }

        if (auth != null && auth == 2) {
            return "일반회원";
        }

        return "알 수 없음";
    }
    
    private Long accountNo;

    private String accountStatus;

    private LocalDateTime stopEndAt;

    public String getAccountStatusName() {
        if (accountStatus == null) {
            return "계좌 없음";
        }

        if ("ACTIVE".equals(accountStatus)) {
            return "거래 가능";
        }

        if ("STOP".equals(accountStatus)) {
            return "거래 정지";
        }

        if ("CLOSE".equals(accountStatus)) {
            return "계좌 폐쇄";
        }

        return "알 수 없음";
    }
}