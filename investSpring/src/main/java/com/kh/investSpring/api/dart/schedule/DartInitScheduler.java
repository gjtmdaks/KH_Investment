package com.kh.investSpring.api.dart.schedule;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.kh.investSpring.api.dart.service.DartCorpCodeService;
import com.kh.investSpring.api.dart.service.DartMinorityShareholderService;
import com.kh.investSpring.api.dart.service.DartStockService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DartInitScheduler {

    private final DartCorpCodeService dartCorpCodeService;
    private final DartStockService dartStockService;
    private final DartMinorityShareholderService dartMinorityShareholderService;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {

//        try {
//            dartCorpCodeService.syncCorpCodes();
//        } catch (Exception e) {
//            log.error("corpCode 동기화 실패", e);
//        }
//
//        try {
//            dartStockService.syncStockTotals();
//        } catch (Exception e) {
//            log.error("주식수 동기화 실패", e);
//        }
//
//        try {
//        	dartMinorityShareholderService.syncMinorityShareholders();
//        } catch (Exception e) {
//            log.error("주식비율 동기화 실패", e);
//        }
    }
}