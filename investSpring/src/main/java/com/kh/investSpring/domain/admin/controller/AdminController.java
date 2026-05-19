package com.kh.investSpring.domain.admin.controller;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.investSpring.api.kis.service.KisHistoryService;
import com.kh.investSpring.domain.admin.dto.AdminStatusUpdateRequest;
import com.kh.investSpring.domain.admin.dto.AdminUserListResponse;
import com.kh.investSpring.domain.admin.dto.AdminUserSearchRequest;
import com.kh.investSpring.domain.admin.service.AdminService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
	
    private final AdminService adminService;
    private final KisHistoryService kisHistoryService;
    
    private final AtomicBoolean companySyncRunning = new AtomicBoolean(false);
    private final AtomicBoolean historySyncRunning = new AtomicBoolean(false);
    private final AtomicBoolean historySyncStopRunning = new AtomicBoolean(false);

    /**
     * 회사 정보 전체 동기화
     */
    @PostMapping("/api/dart/init")
    public void init() {
        log.info("회사 정보 동기화 시작");
    	adminService.initAllData(companySyncRunning);
    }

    /**
     * 과거 시세 전체 동기화
     */
    @PostMapping("/api/kis/historysync")
    public void syncHistory() {
        try {
            log.info("과거 시세 동기화 시작");
            kisHistoryService.syncAllHistory(historySyncRunning);

        } catch (Exception e) {
            log.error("과거 시세 동기화 실패", e);
        }
    }

    /**
     * 과거 시세 동기화 중지
     */
    @PostMapping("/api/kis/historysyncstop")
    public void syncHistoryStop() {
        try {
            log.info("과거 시세 동기화 중지 시작");
            kisHistoryService.syncHistoryStop(historySyncStopRunning);

        } catch (Exception e) {
            log.error("과거 시세 동기화 중지 실패", e);
        }
    }
    
    @GetMapping("/api/users")
    public AdminUserListResponse selectAdminUserList(
            @ModelAttribute AdminUserSearchRequest request
    ) {
        return adminService.selectAdminUserList(request);
    }
    
    /**
     * 회원 계좌 상태 변경
     * ACTIVE: 정상
     * STOP: 거래 정지
     * CLOSE: 계좌 폐쇄
     */
    @PatchMapping("/api/users/{userNo}/account-status")
    public void updateAdminUserAccountStatus(
            @PathVariable Long userNo,
            @RequestBody AdminStatusUpdateRequest request
    ) {
        adminService.updateAdminUserAccountStatus(userNo, request);
    }

    /**
     * 회원 상태 변경
     * ACTIVE: 정상/복구
     * DELETE: 삭제 처리
     */
    @PatchMapping("/api/users/{userNo}/status")
    public void updateAdminUserStatus(
            @PathVariable Long userNo,
            @RequestBody AdminStatusUpdateRequest request
    ) {
        adminService.updateAdminUserStatus(userNo, request);
    }
}