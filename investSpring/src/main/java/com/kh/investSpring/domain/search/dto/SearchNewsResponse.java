package com.kh.investSpring.domain.search.dto;

import lombok.Data;

@Data
public class SearchNewsResponse {

    private Long newsInfoId;
    private String newsTitle;
    private String newsDescription;
    private String publishedAt;
    private String articleLink;
    private String publisher;
}