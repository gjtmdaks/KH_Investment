package com.kh.investSpring.domain.main.controller;

import org.springframework.web.bind.annotation.*;

import com.kh.investSpring.domain.main.dto.MainResponse;
import com.kh.investSpring.domain.main.service.MainService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
public class MainController {

    private final MainService mainService;

    @GetMapping
    public MainResponse getMain(@RequestParam Long userNo) {
        return mainService.getMain(userNo);
    }
}