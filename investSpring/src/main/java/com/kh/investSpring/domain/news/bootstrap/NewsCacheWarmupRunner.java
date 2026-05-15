package com.kh.investSpring.domain.news.bootstrap;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.kh.investSpring.domain.news.service.NewsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 기동 직후 시장 뉴스 Redis 캐시를 미리 채워 첫 화면 지연을 줄입니다.
 * 본문은 동기 블로킹하지 않도록 {@code newsTaskExecutor}에서 실행합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NewsCacheWarmupRunner implements ApplicationRunner {

	private static final int WARM_SIZE = 100;

	private final NewsService newsService;

	@Qualifier("newsTaskExecutor")
	private final Executor newsTaskExecutor;

	@Override
	public void run(ApplicationArguments args) {
		newsTaskExecutor.execute(() -> {
			try {
				newsService.getMarketNews(WARM_SIZE);
				log.info("뉴스 시장 캐시 워밍 완료 (size={})", WARM_SIZE);
			} catch (Exception e) {
				log.warn("뉴스 시장 캐시 워밍 실패: {}", e.getMessage());
			}
		});
	}
}
