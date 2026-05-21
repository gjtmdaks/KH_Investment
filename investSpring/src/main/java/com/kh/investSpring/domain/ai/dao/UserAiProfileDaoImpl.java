package com.kh.investSpring.domain.ai.dao;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.investSpring.domain.ai.dto.UserAiProfileDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserAiProfileDaoImpl implements UserAiProfileDao {

    private final SqlSessionTemplate session;

    @Override
    public UserAiProfileDto findByUserNo(Long userNo) {
        return session.selectOne("ai.findUserAiProfile", userNo);
    }

    @Override
    public int insert(UserAiProfileDto dto) {
        return session.insert("ai.insertUserAiProfile", dto);
    }

    @Override
    public int update(UserAiProfileDto dto) {
        return session.update("ai.updateUserAiProfile", dto);
    }
    
    @Override
    public boolean exists(Long userNo) {
        Integer count = session.selectOne("ai.existsUserAiProfile", userNo);

        return count != null && count > 0;
    }
}