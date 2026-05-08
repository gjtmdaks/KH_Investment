package com.kh.investSpring.api.dart.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Getter
@Configuration
public class DartProperties {

    @Value("${dart.api.appkey}")
    private String appKey;
}