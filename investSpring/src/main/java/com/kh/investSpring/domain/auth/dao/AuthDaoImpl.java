package com.kh.investSpring.domain.auth.dao;

import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AuthDaoImpl implements AuthDao {

	private static final String NS = "com.kh.investSpring.domain.auth.dao.AuthDao.";

	private final SqlSessionTemplate session;

	@Override
	public Long findUserNoByKakaoProviderId(String providerId) {
		return session.selectOne(NS + "findUserNoByKakaoProviderId", Map.of("providerId", providerId));
	}

	@Override
	public long nextUserNo() {
		return session.selectOne(NS + "nextUserNo");
	}

	@Override
	public void insertKakaoUser(long userNo, String displayName) {
		session.insert(NS + "insertKakaoUser", Map.of("userNo", userNo, "displayName", displayName));
	}

	@Override
	public void insertUserSocial(String providerId, long userNo) {
		session.insert(NS + "insertUserSocial", Map.of("providerId", providerId, "userNo", userNo));
	}
}
