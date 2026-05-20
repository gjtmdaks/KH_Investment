package com.kh.investSpring.api.kis.http;

import java.util.Map;

public record KisOrderbookHttpResponse(
        String rt_cd,
        String msg_cd,
        String msg1,
        Map<String, Object> output1,
        Map<String, Object> output2) {
}
