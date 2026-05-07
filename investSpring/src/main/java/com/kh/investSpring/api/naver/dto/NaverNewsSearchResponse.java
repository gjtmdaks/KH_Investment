package com.kh.investSpring.api.naver.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverNewsSearchResponse(List<NaverNewsItemDto> items) {
}
