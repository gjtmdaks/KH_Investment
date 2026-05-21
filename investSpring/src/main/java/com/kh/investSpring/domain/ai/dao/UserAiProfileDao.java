package com.kh.investSpring.domain.ai.dao;

import com.kh.investSpring.domain.ai.dto.UserAiProfileDto;

public interface UserAiProfileDao {
	
	UserAiProfileDto findByUserNo(Long userNo);
	
	int insert(UserAiProfileDto dto);
	
	int update(UserAiProfileDto dto);
	
	boolean exists(Long userNo);

}
