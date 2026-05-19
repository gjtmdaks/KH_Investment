package com.kh.investSpring.domain.admin.service;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.kh.investSpring.api.dart.service.DartCorpCodeService;
import com.kh.investSpring.api.dart.service.DartMinorityShareholderService;
import com.kh.investSpring.api.dart.service.DartStockService;
import com.kh.investSpring.domain.admin.dao.AdminDao;
import com.kh.investSpring.domain.admin.dto.AdminStatusUpdateRequest;
import com.kh.investSpring.domain.admin.dto.AdminUserListResponse;
import com.kh.investSpring.domain.admin.dto.AdminUserSearchRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final DartCorpCodeService dartCorpCodeService;
    private final DartStockService dartStockService;
    private final DartMinorityShareholderService dartMinorityShareholderService;
    private final AdminDao adminDao;

    @Async
    public void initAllData(AtomicBoolean running) {
    	
        if (!running.compareAndSet(false, true)) {
            log.warn("이미 초기 적재 실행 중");
            return;
        }

        try {
        	log.info("기업코드 동기화 시작");
            dartCorpCodeService.syncCorpCodes();
            log.info("기업코드 동기화 완료");
        } catch (Exception e) {
            log.error("corp sync 실패", e);
        }

        try {
        	log.info("발행주식수 동기화 시작");
            dartStockService.syncStockTotals();
            log.info("발행주식수 동기화 완료");
        } catch (Exception e) {
            log.error("stock sync 실패", e);
        }

        try {
        	log.info("소액주주 비율 동기화 시작");
            dartMinorityShareholderService.syncMinorityShareholders();
            log.info("소액주주 비율 동기화 완료");
        } catch (Exception e) {
            log.error("minority sync 실패", e);
        } finally {
            running.set(false);
        }
    }

    public AdminUserListResponse selectAdminUserList(AdminUserSearchRequest request) {
        AdminUserListResponse response = new AdminUserListResponse();

        response.setUsers(adminDao.selectAdminUserList(request));
        response.setTotalCount(adminDao.selectAdminUserTotalCount(request));
        response.setActiveCount(adminDao.selectAdminUserActiveCount(request));
        response.setStopCount(adminDao.selectAdminUserStopCount(request));
        response.setDeleteCount(adminDao.selectAdminUserDeleteCount(request));

        return response;
    }

    public void updateAdminUserAccountStatus(Long userNo, AdminStatusUpdateRequest request) {
        if (userNo == null) {
            throw new IllegalArgumentException("회원 번호가 없습니다.");
        }

        if (request == null || request.getStatus() == null || request.getStatus().isBlank()) {
            throw new IllegalArgumentException("계좌 상태값이 없습니다.");
        }

        String status = request.getStatus().trim().toUpperCase();

        if (!"ACTIVE".equals(status) && !"STOP".equals(status) && !"CLOSE".equals(status)) {
            throw new IllegalArgumentException("허용되지 않는 계좌 상태입니다.");
        }

        if ("STOP".equals(status) && request.getStopEndAt() == null) {
            throw new IllegalArgumentException("거래 정지 종료일이 필요합니다.");
        }

        int result = adminDao.updateAdminUserAccountStatus(
                userNo,
                status,
                request.getStopEndAt()
        );

        if (result == 0) {
            throw new IllegalArgumentException("변경할 계좌를 찾을 수 없습니다.");
        }
    }

    public void updateAdminUserStatus(Long userNo, AdminStatusUpdateRequest request) {
        if (userNo == null) {
            throw new IllegalArgumentException("회원 번호가 없습니다.");
        }

        if (request == null || request.getStatus() == null || request.getStatus().isBlank()) {
            throw new IllegalArgumentException("회원 상태값이 없습니다.");
        }

        String status = request.getStatus().trim().toUpperCase();

        if (!"ACTIVE".equals(status) && !"DELETE".equals(status)) {
            throw new IllegalArgumentException("허용되지 않는 회원 상태입니다.");
        }

        int result = adminDao.updateAdminUserStatus(userNo, status);

        if (result == 0) {
            throw new IllegalArgumentException("변경할 회원을 찾을 수 없습니다.");
        }
    }
}