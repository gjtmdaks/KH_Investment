package com.kh.investSpring.domain.admin.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminStatusUpdateRequest {

    private String status;
    
    private LocalDate stopEndAt;
}