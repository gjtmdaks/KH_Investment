package com.kh.investSpring.api.dart.dto;

import lombok.Data;

@Data
public class MinorityShareholderDto {

    private String corpCode;
    private String stockCode;

    // 주주 비율
    private Double minorityShareholderRatio;

    // 보유 주식 비율
    private Double minorityOwnershipRatio;
}