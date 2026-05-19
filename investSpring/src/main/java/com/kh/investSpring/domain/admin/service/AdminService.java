package com.kh.investSpring.domain.admin.service;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.kh.investSpring.api.dart.service.DartCorpCodeService;
import com.kh.investSpring.api.dart.service.DartMinorityShareholderService;
import com.kh.investSpring.api.dart.service.DartStockService;
import com.kh.investSpring.domain.admin.dao.AdminDao;
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
}