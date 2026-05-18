package com.kh.investSpring.domain.notice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.investSpring.domain.notice.dto.NoticeCreateRequest;
import com.kh.investSpring.domain.notice.dto.NoticeDetailResponse;
import com.kh.investSpring.domain.notice.dto.NoticePageResponse;
import com.kh.investSpring.domain.notice.service.NoticeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {

    private final NoticeService service;

    @GetMapping
    public ResponseEntity<NoticePageResponse> getNoticeList(@RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(service.getNoticeList(page));
    }

    @GetMapping("/{noticeId}")
    public ResponseEntity<NoticeDetailResponse> getNoticeDetail(@PathVariable Long noticeId) {
        return ResponseEntity.ok(service.getNoticeDetail(noticeId));
    }

    @PostMapping
    public ResponseEntity<Void> createNotice(
            Authentication authentication,
            @RequestBody NoticeCreateRequest request
    ) {
        Long userNo = Long.valueOf(authentication.getName());

        service.createNotice(userNo, request);

        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{noticeId}")
    public ResponseEntity<Void> updateNotice(
            @PathVariable Long noticeId,
            Authentication authentication,
            @RequestBody NoticeCreateRequest request
    ) {
        Long userNo = Long.valueOf(authentication.getName());

        service.updateNotice(noticeId, userNo, request);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{noticeId}")
    public ResponseEntity<Void> deleteNotice(
            @PathVariable Long noticeId,
            Authentication authentication
    ) {
        Long userNo = Long.valueOf(authentication.getName());
        service.deleteNotice(noticeId, userNo);

        return ResponseEntity.ok().build();
    }
}