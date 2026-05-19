package com.kh.investSpring.domain.admin.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.kh.investSpring.domain.admin.dto.AdminUserResponse;
import com.kh.investSpring.domain.admin.dto.AdminUserSearchRequest;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AdminDaoImpl implements AdminDao {

    private final SqlSession session;

    @Override
    public List<AdminUserResponse> selectAdminUserList(AdminUserSearchRequest request) {
        return session.selectList("adminMapper.selectAdminUserList", request);
    }

    @Override
    public int selectAdminUserTotalCount(AdminUserSearchRequest request) {
        return session.selectOne("adminMapper.selectAdminUserTotalCount", request);
    }

    @Override
    public int selectAdminUserActiveCount(AdminUserSearchRequest request) {
        return session.selectOne("adminMapper.selectAdminUserActiveCount", request);
    }

    @Override
    public int selectAdminUserStopCount(AdminUserSearchRequest request) {
        return session.selectOne("adminMapper.selectAdminUserStopCount", request);
    }

    @Override
    public int selectAdminUserDeleteCount(AdminUserSearchRequest request) {
        return session.selectOne("adminMapper.selectAdminUserDeleteCount", request);
    }
}