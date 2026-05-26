package com.kh.investSpring.api.kis.http;

public record KisInquireInvestorHttpResponse(
        String rt_cd,
        String msg_cd,
        String msg1,
        Object output) {
}
