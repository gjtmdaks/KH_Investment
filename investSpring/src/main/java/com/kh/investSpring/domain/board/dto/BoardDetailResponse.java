package com.kh.investSpring.domain.board.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardDetailResponse {

    private Long boardNo;

    private Long userNo;

    private String userName;

    private String stockCode;

    private String content;

    private LocalDateTime createdAt;

    private Integer likeCount;

    private Long parentId;

    private String commentType;

    private boolean likedByMe;
}