package com.kh.investSpring;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.kh.investSpring.domain")
public class InvestSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvestSpringApplication.class, args);
	}

}
