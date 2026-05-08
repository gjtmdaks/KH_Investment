package com.kh.investSpring.domain.board.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.kh.investSpring.domain.board.dto.BoardDto;

public interface BoardDao {

    // ✅ 종목별 인기 게시글
    List<BoardDto> getTopPostsByStock(@Param("stockCode") String stockCode);
}