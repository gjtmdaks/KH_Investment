// global/config/WebConfig.java
package com.kh.investSpring.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.kh.investSpring.global.util.JwtUtil;

import jakarta.servlet.Filter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Bean
	public Filter jwtFilter(JwtUtil jwtUtil) {
		return new JwtFilter(jwtUtil);
	}
    
    //@Override
    //public void addCorsMappings(CorsRegistry registry) {
    //    registry.addMapping("/**")
    //            .allowedOrigins("http://localhost:3000")
    //            .allowedMethods("*");
    //}
}