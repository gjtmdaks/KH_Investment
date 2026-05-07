package com.kh.investSpring.domain.user.dao;

import com.kh.investSpring.domain.user.vo.LocalUser;
import com.kh.investSpring.domain.user.vo.User;

public interface UserDao {
	
	int selectByUserId(String userId);
    int insertUser(User user);
    int insertLocalUser(LocalUser localUser);
    
    
}
