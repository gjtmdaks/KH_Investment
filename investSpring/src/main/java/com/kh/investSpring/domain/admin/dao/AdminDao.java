package com.kh.investSpring.domain.admin.dao;

import java.util.List;

import com.kh.investSpring.domain.admin.dto.AdminUserResponse;
import com.kh.investSpring.domain.admin.dto.AdminUserSearchRequest;

public interface AdminDao {

    List<AdminUserResponse> selectAdminUserList(AdminUserSearchRequest request);

    int selectAdminUserTotalCount(AdminUserSearchRequest request);

    int selectAdminUserActiveCount(AdminUserSearchRequest request);

    int selectAdminUserStopCount(AdminUserSearchRequest request);

    int selectAdminUserDeleteCount(AdminUserSearchRequest request);

	int updateAdminUserAccountStatus(Long userNo, String status);

	int updateAdminUserStatus(Long userNo, String status);
}