package com.kh.investSpring.domain.board.dto;

import lombok.Getter;

@Getter
public class BoardDto {

    private Long boardNo;
    private String content;
    private Long likeCount;
}