package com.kh.investSpring.domain.user.dao;

import com.kh.investSpring.domain.user.vo.LocalUser;
import com.kh.investSpring.domain.user.vo.User;

public interface UserDao {

	int selectByUserId(String userId);

	User selectUserByUserNo(long userNo);

	int insertUser(User user);

	int insertLocalUser(LocalUser localUser);

	LocalUser selectLocalUserByUserId(String userId);

}
