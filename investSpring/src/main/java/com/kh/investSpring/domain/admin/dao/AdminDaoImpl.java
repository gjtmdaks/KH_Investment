package com.kh.investSpring.domain.admin.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public int updateAdminUserAccountStatus(Long userNo, String status) {
        Map<String, Object> param = new HashMap<>();
        param.put("userNo", userNo);
        param.put("status", status);

        return session.update("adminMapper.updateAdminUserAccountStatus", param);
    }

    @Override
    public int updateAdminUserStatus(Long userNo, String status) {
        Map<String, Object> param = new HashMap<>();
        param.put("userNo", userNo);
        param.put("status", status);

        return session.update("adminMapper.updateAdminUserStatus", param);
    }
}