package com.kh.investSpring.domain.auth.dao;

public interface AuthDao {

	Long findUserNoByKakaoProviderId(String providerId);

	long nextUserNo();

	void insertKakaoUser(long userNo, String displayName);

	void insertUserSocial(String providerId, long userNo);
}
