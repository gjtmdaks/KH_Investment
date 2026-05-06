package com.kh.investSpring.domain.auth.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthMapper {

	Long findUserNoByKakaoProviderId(@Param("providerId") String providerId);

	long nextUserNo();

	int insertKakaoUser(@Param("userNo") long userNo, @Param("displayName") String displayName);

	int insertUserSocial(@Param("providerId") String providerId, @Param("userNo") long userNo);
}
