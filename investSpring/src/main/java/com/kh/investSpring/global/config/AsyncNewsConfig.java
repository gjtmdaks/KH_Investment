package com.kh.investSpring.global.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncNewsConfig {

	@Bean(name = "newsTaskExecutor")
	public Executor newsTaskExecutor() {
		ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
		ex.setCorePoolSize(2);
		ex.setMaxPoolSize(8);
		ex.setQueueCapacity(300);
		ex.setThreadNamePrefix("news-og-");
		ex.initialize();
		return ex;
	}
}
