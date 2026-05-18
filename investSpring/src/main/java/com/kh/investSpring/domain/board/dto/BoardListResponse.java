package com.kh.investSpring.domain.board.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardListResponse {

    private Long boardNo;

    private Long userNo;

    private String userName;

    private String stockCode;

    private String content;

    private LocalDateTime createdAt;

    private Integer likeCount;

    private Long parentId;

    /*
     * COMMENT = 일반 댓글
     * REPLY = 대댓글
     */
    private String commentType;

    /*
     * 현재 로그인 사용자가 좋아요를 눌렀는지 여부
     */
    private boolean likedByMe;
}