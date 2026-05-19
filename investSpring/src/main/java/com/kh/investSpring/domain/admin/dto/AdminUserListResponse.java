package com.kh.investSpring.domain.admin.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserListResponse {

    private List<AdminUserResponse> users;

    private int totalCount;

    private int activeCount;

    private int stopCount;

    private int deleteCount;
}