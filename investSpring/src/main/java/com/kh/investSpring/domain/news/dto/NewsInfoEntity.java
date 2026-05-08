package com.kh.investSpring.domain.news.dto;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewsInfoEntity {

	private Long newsInfoId;
	private String articleLink;
	private String newsTitle;
	private String newsDescription;
	private String publisher;
	private String primaryLabel;
	/**
	 * STOCK | SECTOR | MACRO | ISSUE
	 */
	private String keywordKind;
	private Date publishedAt;
	private Date updatedAt;
}
