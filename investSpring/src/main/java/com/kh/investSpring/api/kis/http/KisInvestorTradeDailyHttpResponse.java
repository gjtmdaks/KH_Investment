package com.kh.investSpring.api.kis.http;

public record KisInvestorTradeDailyHttpResponse(
        String rt_cd,
        String msg_cd,
        String msg1,
        Object output1,
        Object output2) {
}
