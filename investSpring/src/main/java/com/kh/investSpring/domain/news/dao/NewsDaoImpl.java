package com.kh.investSpring.domain.news.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.investSpring.domain.news.dto.NewsInfoEntity;
import com.kh.investSpring.domain.news.dto.NewsRelatedStockRow;
import com.kh.investSpring.domain.news.dto.StockDictionaryEntry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * DB 접근 {@link NewsDao} MyBatis 매퍼를 통해 수행.
 *
 * 매퍼 XML namespace가 인터페이스 FQCN({@code com.kh.investSpring.domain.news.dao.NewsDao})으로
 * 잡혀 있어, statement id 호출도 동일한 prefix를 사용합
 *
 * 다중 인자(여러 {@code @Param})를 갖는 매퍼는 {@code Map<String, Object>}로 묶어 전달.
 * (e.g. {@link #mergeNewsInfoStock(long, String)})
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class NewsDaoImpl implements NewsDao {

	private static final String NS = "com.kh.investSpring.domain.news.dao.NewsDao.";

	private final SqlSessionTemplate session;

	@Override
	public void upsertNewsInfo(NewsInfoEntity row) {
		session.insert(NS + "upsertNewsInfo", row);
	}

	@Override
	public Long selectNewsInfoIdByLink(String articleLink) {
		Map<String, Object> p = new HashMap<>();
		p.put("articleLink", articleLink);
		return session.selectOne(NS + "selectNewsInfoIdByLink", p);
	}

	@Override
	public void mergeNewsInfoStock(long newsInfoId, String stockCode) {
		Map<String, Object> p = new HashMap<>();
		p.put("newsInfoId", newsInfoId);
		p.put("stockCode", stockCode);
		session.insert(NS + "mergeNewsInfoStock", p);
	}

	@Override
	public void updateNewsTitleDescription(long newsInfoId, String newsTitle, String newsDescription) {
		Map<String, Object> p = new HashMap<>();
		p.put("newsInfoId", newsInfoId);
		p.put("newsTitle", newsTitle);
		p.put("newsDescription", newsDescription);
		session.update(NS + "updateNewsTitleDescription", p);
	}

	@Override
	public List<NewsInfoEntity> selectRecentNewsInfo(int limit) {
		Map<String, Object> p = new HashMap<>();
		p.put("limit", limit);
		return session.selectList(NS + "selectRecentNewsInfo", p);
	}

	@Override
	public List<NewsInfoEntity> selectNewsInfoByStockCode(String stockCode, int limit) {
		Map<String, Object> p = new HashMap<>();
		p.put("stockCode", stockCode);
		p.put("limit", limit);
		return session.selectList(NS + "selectNewsInfoByStockCode", p);
	}

	@Override
	public List<StockDictionaryEntry> selectAllActiveStockDictionary() {
		return session.selectList(NS + "selectAllActiveStockDictionary");
	}

	@Override
	public List<NewsRelatedStockRow> selectRelatedStocksByNewsIds(List<Long> ids) {
		Map<String, Object> p = new HashMap<>();
		p.put("ids", ids);
		return session.selectList(NS + "selectRelatedStocksByNewsIds", p);
	}

}
