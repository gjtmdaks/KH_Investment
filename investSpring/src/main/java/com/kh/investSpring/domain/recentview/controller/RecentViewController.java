package com.kh.investSpring.domain.recentview.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.investSpring.domain.recentview.service.RecentViewService;
import com.kh.investSpring.global.common.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/recent-view")
@RequiredArgsConstructor
public class RecentViewController {

    private final RecentViewService service;

    @PostMapping("/{stockCode}")
    public ApiResponse<?> saveRecentView(
            @PathVariable String stockCode,
            HttpServletRequest request
    ) {
        Long userNo = (Long) request.getAttribute("userNo");

        service.saveRecentView(userNo, stockCode);

        return ApiResponse.success(
            null,
            "최근 본 저장 성공"
        );
    }
}