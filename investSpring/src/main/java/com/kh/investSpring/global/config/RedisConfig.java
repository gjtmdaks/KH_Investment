package com.kh.investSpring.global.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {

	@Bean
	public RedissonClient redissonClient(
			@Value("${invest.redis.address:redis://127.0.0.1:6379}") String address) {
		Config config = new Config();
		config.useSingleServer().setAddress(address);
		return Redisson.create(config);
	}

	@Bean
	public RedisConnectionFactory redisConnectionFactory(
			@Value("${spring.data.redis.host:127.0.0.1}") String host,
			@Value("${spring.data.redis.port:6379}") int port) {
		return new LettuceConnectionFactory(host, port);
	}

	@Bean
	public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
		return new StringRedisTemplate(factory);
	}
}
