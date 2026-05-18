package com.kh.investSpring.domain.board.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.kh.investSpring.domain.board.dto.BoardDto;

public interface BoardDao {
	
	List<BoardDto> selectBoardListByStockCode(String stockCode, Long userNo);
	
	int insertBoardPost(BoardDto boardDto);

	BoardDto selectBoardByBoardNo(Long boardNo, Long userNo);

	int updateBoardDeletedYnByBoardNo(Long boardNo);

	int selectBoardLikeCountByBoardNoAndUserNo(Long boardNo, Long userNo);

	int insertBoardLike(Long boardNo, Long userNo);

	int deleteBoardLike(Long boardNo, Long userNo);

	int updateBoardLikeCountIncrease(Long boardNo);

	int updateBoardLikeCountDecrease(Long boardNo);
}