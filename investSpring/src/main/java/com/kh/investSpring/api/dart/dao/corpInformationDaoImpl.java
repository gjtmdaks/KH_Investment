package com.kh.investSpring.api.dart.dao;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.investSpring.api.dart.dto.DartCorpCodeDto;
import com.kh.investSpring.api.dart.dto.StockTotalQuantityDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class corpInformationDaoImpl implements corpInformationDao {
	
	private final SqlSessionTemplate session;

	@Override
	public void mergeCorpCode(DartCorpCodeDto dto) {
		session.update("api.mergeCorpCodes", dto);
	}

	@Override
	public List<Map<String, Object>> selectCorpCodes() {
		return session.selectList("api.selectCorpCodes");
	}

	@Override
	public void updateStockTotals(StockTotalQuantityDto dto) {
		session.update("api.updateStockTotals", dto);
	}

}
