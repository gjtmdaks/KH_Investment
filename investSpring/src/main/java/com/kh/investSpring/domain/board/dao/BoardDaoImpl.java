package com.kh.investSpring.domain.board.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.investSpring.domain.board.dto.BoardDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
@RequiredArgsConstructor
public class BoardDaoImpl implements BoardDao {

    private final SqlSessionTemplate session;
    
    @Override
    public List<BoardDto> selectBoardListByStockCode(String stockCode, Long userNo) {
        Map<String, Object> param = new HashMap<>();
        param.put("stockCode", stockCode);
        param.put("userNo", userNo);

        return session.selectList("board.selectBoardListByStockCode", param);
    }

    @Override
    public int insertBoardPost(BoardDto boardDto) {
        return session.insert("board.insertBoardPost", boardDto);
    }

    @Override
    public BoardDto selectBoardByBoardNo(Long boardNo, Long userNo) {
        Map<String, Object> param = new HashMap<>();
        param.put("boardNo", boardNo);
        param.put("userNo", userNo);

        return session.selectOne("board.selectBoardByBoardNo", param);
    }

    @Override
    public int updateBoardDeletedYnByBoardNo(Long boardNo) {
        return session.update("board.updateBoardDeletedYnByBoardNo", boardNo);
    }

    @Override
    public int selectBoardLikeCountByBoardNoAndUserNo(Long boardNo, Long userNo) {
        Map<String, Object> param = new HashMap<>();
        param.put("boardNo", boardNo);
        param.put("userNo", userNo);

        return session.selectOne("board.selectBoardLikeCountByBoardNoAndUserNo", param);
    }

    @Override
    public int insertBoardLike(Long boardNo, Long userNo) {
        Map<String, Object> param = new HashMap<>();
        param.put("boardNo", boardNo);
        param.put("userNo", userNo);

        return session.insert("board.insertBoardLike", param);
    }

    @Override
    public int deleteBoardLike(Long boardNo, Long userNo) {
        Map<String, Object> param = new HashMap<>();
        param.put("boardNo", boardNo);
        param.put("userNo", userNo);

        return session.delete("board.deleteBoardLike", param);
    }

    @Override
    public int updateBoardLikeCountIncrease(Long boardNo) {
        return session.update("board.updateBoardLikeCountIncrease", boardNo);
    }

    @Override
    public int updateBoardLikeCountDecrease(Long boardNo) {
        return session.update("board.updateBoardLikeCountDecrease", boardNo);
    }

    
}
