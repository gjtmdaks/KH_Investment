package com.kh.investSpring.api.kis.http;

import java.util.List;
import java.util.Map;

public record KisInquireCcnlHttpResponse(
        String rt_cd,
        String msg_cd,
        String msg1,
        List<Map<String, Object>> output) {
}
