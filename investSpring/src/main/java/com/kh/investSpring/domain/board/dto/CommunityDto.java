package com.kh.investSpring.domain.board.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommunityDto {

    private Long boardNo;
    private String content;
    private Long likeCount;
}