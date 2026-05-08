package com.kh.investSpring.domain.news.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.kh.investSpring.domain.news.dao.NewsDao;
import com.kh.investSpring.domain.news.util.ArticleOpenGraphFetcher;
import com.kh.investSpring.domain.news.util.ArticleOpenGraphFetcher.OgPayload;
import com.kh.investSpring.domain.news.util.NewsContentMergeUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * OG 메타 조회 및 DB 제목·요약 보강을 요청 스레드와 분리.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NewsOgEnrichmentService {

	private final ArticleOpenGraphFetcher articleOpenGraphFetcher;
	private final NewsDao newsDao;

	@Async("newsTaskExecutor")
	public void enrichAfterPersist(long newsInfoId, String naverTitle, String naverDescription, String articleLink) {
		if (newsInfoId <= 0 || articleLink == null || articleLink.isBlank()) {
			return;
		}
		try {
			OgPayload og = articleOpenGraphFetcher.fetch(articleLink);
			String title = NewsContentMergeUtil.mergeTitle(naverTitle, og.title());
			String description = NewsContentMergeUtil.mergeDescription(naverDescription, og.description());
			newsDao.updateNewsTitleDescription(newsInfoId, title, description);
		} catch (Exception e) {
			log.debug("OG 후행 보강 실패 newsInfoId={}: {}", newsInfoId, e.getMessage());
		}
	}
}
