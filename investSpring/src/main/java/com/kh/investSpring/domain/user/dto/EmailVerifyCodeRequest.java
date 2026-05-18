package com.kh.investSpring.domain.user.dto;

public record EmailVerifyCodeRequest(String email, String code) {
}
