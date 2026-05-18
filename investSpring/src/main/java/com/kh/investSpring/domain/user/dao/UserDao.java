package com.kh.investSpring.domain.user.dao;

import com.kh.investSpring.domain.user.vo.LocalUser;
import com.kh.investSpring.domain.user.vo.User;

public interface UserDao {

	int selectByUserId(String userId);

	int countActiveUserByEmail(String email);

	User selectUserByUserNo(long userNo);

	int insertUser(User user);

	int insertLocalUser(LocalUser localUser);

	LocalUser selectLocalUserByUserId(String userId);

	int updateUserStatusDelete(Long userNo);

	LocalUser selectLocalUserByUserNo(Long userNo);

	int updateUserInfo(User user);

	LocalUser selectLocalUserByUserIdAndUserName(String userId, String userName);

	String selectLocalUserIdByEmail(String email);

	LocalUser selectLocalUserByUserIdAndUserNameAndEmail(String userId, String userName, String email);

	int updatePassword(LocalUser localUser);

	void deleteInvestmentType(Long userNo);

	int insertInvestmentType(Long userNo, int calculatedTotalPoint, String resultFile);

	Integer selectInvestmentTotalPointByUserNo(Long userNo);

	

	
}
