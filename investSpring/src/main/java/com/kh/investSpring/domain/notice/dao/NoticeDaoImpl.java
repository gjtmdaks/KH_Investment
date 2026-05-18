package com.kh.investSpring.domain.notice.dao;

import java.util.List;

import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.investSpring.domain.notice.dto.NoticeDetailResponse;
import com.kh.investSpring.domain.notice.dto.NoticeListResponse;
import com.kh.investSpring.domain.notice.vo.Notice;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NoticeDaoImpl implements NoticeDao {

    private final SqlSessionTemplate session;

    @Override
    public List<NoticeListResponse> selectNoticeList(RowBounds rowBounds) {
        return session.selectList("notice.selectNoticeList", rowBounds);
    }

    @Override
    public long selectNoticeCount() {
        return session.selectOne("notice.selectNoticeCount");
    }

    @Override
    public NoticeDetailResponse selectNoticeDetail(Long noticeId) {
        return session.selectOne("notice.selectNoticeDetail", noticeId);
    }

    @Override
    public int insertNotice(Notice notice) {
        return session.insert("notice.insertNotice", notice);
    }
    
    @Override
    public int updateNotice(Notice notice) {
        return session.update("notice.updateNotice", notice);
    }

    @Override
    public int deleteNotice(Long noticeId) {
        return session.update("notice.deleteNotice", noticeId);
    }

	@Override
	public void increaseViewCount(Long noticeId) {
		session.update("notice.increaseViewCount", noticeId);
	}
}