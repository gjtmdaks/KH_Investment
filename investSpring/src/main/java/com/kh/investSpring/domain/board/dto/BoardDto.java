package com.kh.investSpring.domain.board.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BoardDto {

    private Long boardNo;

    private Long userNo;

    private String userName;

    private String stockCode;

    private String content;

    private LocalDateTime createdAt;

    private Integer likeCount;

    private Long parentId;

    private String deletedYn;

    private LocalDateTime deletedAt;

    /*
     * 로그인한 사용자가 좋아요를 눌렀으면 Y
     * 아니면 N
     */
    private String likedByMe;
}