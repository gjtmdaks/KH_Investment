package com.kh.investSpring.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSummaryDto {

    private Long userNo;
    private String userName;
}