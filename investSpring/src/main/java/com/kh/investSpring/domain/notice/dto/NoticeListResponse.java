package com.kh.investSpring.domain.notice.dto;

import java.util.Date;

import lombok.Data;

@Data
public class NoticeListResponse {

    private Long noticeId;
    private String title;
    private Date createdAt;
    private Date updatedAt;
}