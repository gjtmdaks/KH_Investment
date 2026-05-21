package com.kh.investSpring.domain.account.dto;

import lombok.Data;

@Data
public class RankingResponse {
	
	private Integer rank;
	
	private Long userNo;
	
	private String userName;
	
	private Long evaluationAmount;
	
	private Double profitRate;
	
	public enum RankingType {
	    total,
	    oneday
	}

}
