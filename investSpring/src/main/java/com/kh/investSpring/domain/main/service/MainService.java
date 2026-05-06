package com.kh.investSpring.domain.main.service;

import com.kh.investSpring.domain.main.dto.MainResponse;

public interface MainService {
    MainResponse getMain(Long userNo);
}