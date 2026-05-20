package com.kh.investSpring.domain.account.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountTradeStatusResponse {

    private String status;

    private LocalDateTime stopEndAt;
}
