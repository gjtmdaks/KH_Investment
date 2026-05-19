package com.kh.investSpring.domain.main.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.investSpring.domain.main.dto.MainResponse;
import com.kh.investSpring.domain.main.service.MainService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
public class MainController {

    private final MainService mainService;

    @GetMapping
    public MainResponse getMain(HttpServletRequest request) {
        Long userNo = (Long) request.getAttribute("userNo");
        return mainService.getMain(userNo);
    }
}