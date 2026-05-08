package com.kh.investSpring.domain.board.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.kh.investSpring.domain.board.dto.BoardDto;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class BoardDaoImpl implements BoardDao{

	@Override
	public List<BoardDto> getTopPostsByStock(String stockCode) {
		// TODO Auto-generated method stub
		return null;
	}

}
