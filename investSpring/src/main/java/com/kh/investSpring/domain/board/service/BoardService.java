package com.kh.investSpring.domain.board.service;

import java.util.List;

import com.kh.investSpring.domain.board.dto.BoardCreateRequest;
import com.kh.investSpring.domain.board.dto.BoardListResponse;

public interface BoardService {

    List<BoardListResponse> selectBoardListByStockCode(String stockCode, Long userNo);

    BoardListResponse insertBoardPost(
            String stockCode,
            Long userNo,
            BoardCreateRequest request
    );

    void updateBoardDeletedYnByBoardNo(Long boardNo, Long userNo);

    BoardListResponse insertBoardLike(Long boardNo, Long userNo);

    BoardListResponse deleteBoardLike(Long boardNo, Long userNo);
}