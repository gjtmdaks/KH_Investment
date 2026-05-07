package com.kh.investSpring;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.kh.investSpring.domain", annotationClass = Mapper.class)
public class InvestSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvestSpringApplication.class, args);
	}

}
