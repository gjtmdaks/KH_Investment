package com.kh.investSpring.domain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsAnalysisTargetDto {

    private Long newsInfoId;

    private String title;

    private String description;
}