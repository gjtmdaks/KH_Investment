package com.kh.investSpring.domain.account.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kh.investSpring.domain.account.service.AccountService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountSnapshotScheduler {
	
	private final AccountService accountService;

    // 평일 오전 9시 30분마다 전일 기준 자산 갱신
    @Scheduled(cron = "0 0 0 * * MON-FRI", zone = "Asia/Seoul")
    public void updatePreviousTotalAsset() {
        int updatedCount =
                accountService.updatePreviousTotalAssetForAllActiveAccounts();

        log.info("전일 기준 자산 갱신 완료. updatedCount={}", updatedCount);
    }
}
