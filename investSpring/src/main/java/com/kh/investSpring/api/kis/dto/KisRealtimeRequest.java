package com.kh.investSpring.api.kis.dto;

public record KisRealtimeRequest(
        Header header,
        Body body
) {

    public record Header(
            String approval_key,
            String custtype,
            String tr_type,
            String content_type
    ) {}

    public record Body(
            Input input
    ) {}

    public record Input(
            String tr_id,
            String tr_key
    ) {}
}