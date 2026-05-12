package com.kh.investSpring.domain.user.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InvestmentTypeSaveRequest {

    private int totalPoint;
    private String resultType;
    private List<InvestmentTypeAnswerRequest> answers;
}
