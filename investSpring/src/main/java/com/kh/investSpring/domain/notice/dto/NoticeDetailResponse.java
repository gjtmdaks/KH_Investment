package com.kh.investSpring.domain.notice.dto;

import java.util.Date;

import lombok.Data;

@Data
public class NoticeDetailResponse {

    private Long noticeId;
    private String title;
    private String content;
    private Date createdAt;
    private Date updatedAt;
    private Long userNo;
}