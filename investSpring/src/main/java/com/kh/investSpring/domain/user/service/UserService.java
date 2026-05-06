package com.kh.investSpring.domain.user.service;

import com.kh.investSpring.domain.user.dto.UserSummaryDto;

public interface UserService {

    // ✅ 메인 헤더용
    UserSummaryDto getUser(Long userNo);
}