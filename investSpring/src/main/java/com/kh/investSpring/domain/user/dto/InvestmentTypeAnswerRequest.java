package com.kh.investSpring.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InvestmentTypeAnswerRequest {

    private int questionNo;
    private String questionText;
    private int optionNo;
    private String optionText;
    private int point;
}
