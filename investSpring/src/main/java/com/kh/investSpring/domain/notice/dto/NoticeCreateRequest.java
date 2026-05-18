package com.kh.investSpring.domain.notice.dto;

import lombok.Data;

@Data
public class NoticeCreateRequest {

    private String title;
    private String content;
}