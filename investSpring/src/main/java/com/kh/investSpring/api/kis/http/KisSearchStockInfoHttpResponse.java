package com.kh.investSpring.api.kis.http;

import java.util.Map;

public record KisSearchStockInfoHttpResponse(
        String rt_cd,
        String msg_cd,
        String msg1,
        Map<String, Object> output) {
}
