package com.kh.investSpring.domain.user.service;

import com.kh.investSpring.domain.main.dto.MainResponse.Header;

public interface UserService {

    // ✅ 메인 헤더용
	Header getHeader(Long userNo);
}