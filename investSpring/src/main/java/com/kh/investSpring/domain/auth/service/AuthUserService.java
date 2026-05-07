package com.kh.investSpring.domain.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.investSpring.domain.auth.dao.AuthDao;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthUserService {

	private final AuthDao authDao;

	@Transactional
	public Long resolveOrCreateKakaoUser(String providerId, String displayName) {
		Long userNo = authDao.findUserNoByKakaoProviderId(providerId);
		if (userNo != null) {
			return userNo;
		}
		long newUserNo = authDao.nextUserNo();
		authDao.insertKakaoUser(newUserNo, displayName);
		authDao.insertUserSocial(providerId, newUserNo);
		return newUserNo;
	}
}
