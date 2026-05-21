package com.kh.investSpring.domain.ai.service;

import com.kh.investSpring.domain.ai.dto.UserAiProfileDto;

public interface UserAiProfileService {

    UserAiProfileDto getUserProfile(Long userNo);
    
    void analyzeUserProfile(Long userNo);
}