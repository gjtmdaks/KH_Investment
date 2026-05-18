package com.kh.investSpring.domain.notice.vo;

import java.util.Date;

import lombok.Data;

@Data
public class Notice {

    private Long noticeId;
    private String title;
    private String content;
    private Date createdAt;
    private Date updatedAt;
    private Long userNo;
}