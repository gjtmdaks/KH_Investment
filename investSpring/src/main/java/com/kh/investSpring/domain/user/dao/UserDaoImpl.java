package com.kh.investSpring.domain.user.dao;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.investSpring.domain.user.vo.LocalUser;
import com.kh.investSpring.domain.user.vo.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
@RequiredArgsConstructor
public class UserDaoImpl implements UserDao {
	
	private final SqlSessionTemplate session;
	
	@Override
	public int selectByUserId(String userId) {
		// TODO Auto-generated method stub
		return session.selectOne("user.selectByUserId", userId);
	}

	@Override
	public User selectUserByUserNo(long userNo) {
		return session.selectOne("user.selectUserByUserNo", userNo);
	}

	@Override
	public int insertUser(User user) {
		// TODO Auto-generated method stub
		return session.insert("user.insertUser", user);
	}

	@Override
	public int insertLocalUser(LocalUser localUser) {
		// TODO Auto-generated method stub
		return session.insert("user.insertLocalUser", localUser);
	}

	@Override
	public LocalUser selectLocalUserByUserId(String userId) {
		// TODO Auto-generated method stub
		return session.selectOne("user.selectLocalUserByUserId", userId);
	}

	@Override
	public int updateUserStatusDelete(Long userNo) {
		// TODO Auto-generated method stub
		return session.update("user.updateUserStatusDelete", userNo);
	}

}
