package com.kh.investSpring.domain.notice.service;

import com.kh.investSpring.domain.notice.dto.NoticeCreateRequest;
import com.kh.investSpring.domain.notice.dto.NoticeDetailResponse;
import com.kh.investSpring.domain.notice.dto.NoticePageResponse;

public interface NoticeService {

    NoticePageResponse getNoticeList(int page);

    NoticeDetailResponse getNoticeDetail(Long noticeId);

    void createNotice(Long userNo, NoticeCreateRequest request);
    
    void updateNotice(Long noticeId, Long userNo, NoticeCreateRequest request);

    void deleteNotice(Long noticeId, Long userNo);
}