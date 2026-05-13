package com.kh.investSpring.domain.watchlist.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.investSpring.domain.watchlist.dto.WatchlistResponse;
import com.kh.investSpring.domain.watchlist.service.WatchlistService;
import com.kh.investSpring.global.common.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService service;

    @PostMapping("/{stockCode}")
    public ApiResponse<?> insertWatchlist(@PathVariable String stockCode, HttpServletRequest request) {
        Long userNo = (Long) request.getAttribute("userNo");

        service.insertWatchlist(userNo, stockCode);

        return ApiResponse.success(null, "관심종목 추가 성공");
    }

    @DeleteMapping("/{stockCode}")
    public ApiResponse<?> deleteWatchlist(@PathVariable String stockCode, HttpServletRequest request) {
        Long userNo = (Long) request.getAttribute("userNo");

        service.deleteWatchlist(userNo, stockCode);

        return ApiResponse.success(null, "관심종목 삭제 성공");
    }

    @GetMapping
    public ApiResponse<WatchlistResponse> getWatchlist(HttpServletRequest request) {
        Long userNo = (Long) request.getAttribute("userNo");

        WatchlistResponse response = service.getWatchlist(userNo);

        return ApiResponse.success(response, "관심종목 조회 성공");
    }
}