package com.kh.investSpring.domain.news.bootstrap;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.kh.investSpring.domain.news.service.NewsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 기동 직후 시장 뉴스 Redis 캐시를 미리 채워 첫 화면 지연을 줄입니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NewsCacheWarmupRunner implements ApplicationRunner {

	private static final int WARM_SIZE = 50;

	private final NewsService newsService;

	@Override
	public void run(ApplicationArguments args) {
		try {
			newsService.getMarketNews(WARM_SIZE);
			log.info("뉴스 시장 캐시 워밍 완료 (size={})", WARM_SIZE);
		} catch (Exception e) {
			log.warn("뉴스 시장 캐시 워밍 실패: {}", e.getMessage());
		}
	}
}
