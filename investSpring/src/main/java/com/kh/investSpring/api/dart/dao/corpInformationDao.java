package com.kh.investSpring.api.dart.dao;

import java.util.List;
import java.util.Map;

import com.kh.investSpring.api.dart.dto.DartCorpCodeDto;
import com.kh.investSpring.api.dart.dto.MinorityShareholderDto;
import com.kh.investSpring.api.dart.dto.StockTotalQuantityDto;

public interface corpInformationDao {

	void mergeCorpCode(DartCorpCodeDto dto);
	
	List<Map<String, Object>> selectCorpCodes();

	void updateStockTotals(StockTotalQuantityDto dto);

	void updateMinorityShareholder(MinorityShareholderDto dto);

}
