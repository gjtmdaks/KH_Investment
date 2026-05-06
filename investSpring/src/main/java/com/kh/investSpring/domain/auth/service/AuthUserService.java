package com.kh.investSpring.domain.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.investSpring.domain.auth.mapper.AuthMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthUserService {

	private final AuthMapper authMapper;

	@Transactional
	public Long resolveOrCreateKakaoUser(String providerId, String displayName) {
		Long userNo = authMapper.findUserNoByKakaoProviderId(providerId);
		if (userNo != null) {
			return userNo;
		}
		long newUserNo = authMapper.nextUserNo();
		authMapper.insertKakaoUser(newUserNo, displayName);
		authMapper.insertUserSocial(providerId, newUserNo);
		return newUserNo;
	}
}
