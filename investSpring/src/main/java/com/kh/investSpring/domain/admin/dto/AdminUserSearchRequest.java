package com.kh.investSpring.domain.admin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserSearchRequest {
	private String keyword;

    private String status;

    private Integer auth;
}
