package com.kh.investSpring.domain.notice.service;

import java.util.List;

import org.apache.ibatis.session.RowBounds;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.kh.investSpring.domain.notice.dao.NoticeDao;
import com.kh.investSpring.domain.notice.dto.NoticeCreateRequest;
import com.kh.investSpring.domain.notice.dto.NoticeDetailResponse;
import com.kh.investSpring.domain.notice.dto.NoticeListResponse;
import com.kh.investSpring.domain.notice.dto.NoticePageResponse;
import com.kh.investSpring.domain.notice.vo.Notice;
import com.kh.investSpring.domain.user.dao.UserDao;
import com.kh.investSpring.domain.user.vo.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeDao dao;
    private final UserDao userDao;
    private static final int PAGE_SIZE = 10;

    @Override
    public NoticePageResponse getNoticeList(int page) {
        int offset = (page - 1) * PAGE_SIZE;

        RowBounds rowBounds = new RowBounds(offset, PAGE_SIZE);
        List<NoticeListResponse> notices = dao.selectNoticeList(rowBounds);

        long totalCount = dao.selectNoticeCount();
        int totalPage = Math.max(1, (int) Math.ceil((double) totalCount / PAGE_SIZE));

        return NoticePageResponse.builder()
                .notices(notices)
                .currentPage(page)
                .totalPage(totalPage)
                .totalCount(totalCount)
                .build();
    }

    @Override
    public NoticeDetailResponse getNoticeDetail(Long noticeId) {
    	NoticeDetailResponse notice = dao.selectNoticeDetail(noticeId);

    	if (notice == null) {
    		throw new ResponseStatusException(
    		        HttpStatus.NOT_FOUND,
    		        "공지사항이 존재하지 않습니다."
    				);
    	}
        return notice;
    }

    @Override
    @Transactional
    public void createNotice(Long userNo, NoticeCreateRequest request) {
    	User user = userDao.selectUserByUserNo(userNo);

        if (user == null || user.getAuth() != 1) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "관리자만 작성 가능합니다."
            );
        }

        Notice notice = new Notice();
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setUserNo(userNo);

        dao.insertNotice(notice);
    }
    
    @Override
    @Transactional
    public void updateNotice(Long noticeId, Long userNo, NoticeCreateRequest request) {
    	User user = userDao.selectUserByUserNo(userNo);

        if (user == null || user.getAuth() != 1) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "관리자만 수정 가능합니다."
            );
        }
        
        Notice notice = new Notice();
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setUserNo(userNo);
        notice.setNoticeId(noticeId);

        dao.updateNotice(notice);
    }

    @Override
    @Transactional
    public void deleteNotice(Long noticeId, Long userNo) {
    	User user = userDao.selectUserByUserNo(userNo);

        if (user == null || user.getAuth() != 1) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "관리자만 삭제 가능합니다."
            );
        }

        dao.deleteNotice(noticeId);
    }
}