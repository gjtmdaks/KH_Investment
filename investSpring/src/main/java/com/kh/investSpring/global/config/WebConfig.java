// global/config/WebConfig.java
package com.kh.investSpring.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.Filter;

@Configuration
public class WebConfig {

    @Bean
    public Filter jwtFilter() {
        return new JwtFilter();
    }
}