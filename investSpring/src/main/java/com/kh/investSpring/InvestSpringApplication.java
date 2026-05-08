package com.kh.investSpring;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
@MapperScan(basePackages = "com.kh.investSpring", annotationClass = Mapper.class)
public class InvestSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvestSpringApplication.class, args);
	}

}
