package com.kh.investSpring.domain.admin.dao;

import java.time.LocalDate;
import java.util.List;

import com.kh.investSpring.domain.admin.dto.AdminUserResponse;
import com.kh.investSpring.domain.admin.dto.AdminUserSearchRequest;

public interface AdminDao {

    List<AdminUserResponse> selectAdminUserList(AdminUserSearchRequest request);

    int selectAdminUserTotalCount(AdminUserSearchRequest request);

    int selectAdminUserActiveCount(AdminUserSearchRequest request);

    int selectAdminUserStopCount(AdminUserSearchRequest request);

    int selectAdminUserDeleteCount(AdminUserSearchRequest request);

	int updateAdminUserAccountStatus(Long userNo, String status, LocalDate localDate);

	int updateAdminUserStatus(Long userNo, String status);
}