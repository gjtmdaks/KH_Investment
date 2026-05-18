package com.kh.investSpring.domain.user.dto;

public record FindPasswordRequest(
        String userId,
        String userName,
        String email
) {
}
