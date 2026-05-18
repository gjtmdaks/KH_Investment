package com.kh.investSpring.domain.notice.dao;

import java.util.List;

import org.apache.ibatis.session.RowBounds;

import com.kh.investSpring.domain.notice.dto.NoticeDetailResponse;
import com.kh.investSpring.domain.notice.dto.NoticeListResponse;
import com.kh.investSpring.domain.notice.vo.Notice;

public interface NoticeDao {

    List<NoticeListResponse> selectNoticeList(RowBounds rowBounds);

    long selectNoticeCount();

    NoticeDetailResponse selectNoticeDetail(Long noticeId);

    int insertNotice(Notice notice);
    
    int updateNotice(Notice notice);

    int deleteNotice(Long noticeId);

	void increaseViewCount(Long noticeId);
}