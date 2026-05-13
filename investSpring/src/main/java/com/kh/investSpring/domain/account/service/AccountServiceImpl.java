package com.kh.investSpring.domain.account.service;

import org.springframework.stereotype.Service;

import com.kh.investSpring.domain.account.dao.AccountDao;
import com.kh.investSpring.domain.account.dto.AccountSummaryDto;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;


@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountDao accountDao;
    private static final BigDecimal BASE_CAPITAL = BigDecimal.valueOf(10000000); // 기본자산은 고정
    
    // 전일(장시작) 돈 자동저장
    @Override
	public int updatePreviousTotalAssetForAllActiveAccounts() {
		// TODO Auto-generated method stub
		return accountDao.updatePreviousTotalAssetForAllActiveAccounts();
	}
    
    @Override
    public AccountSummaryDto getAccountSummary(Long userNo) {
        AccountSummaryDto accountSummary =
                accountDao.selectAccountSummaryByUserNo(userNo);

        if (accountSummary == null) {
            return null;
        }

        BigDecimal currentTotalAsset = accountSummary.getCurrentTotalAsset();

        BigDecimal previousTotalAsset = accountSummary.getPreviousTotalAsset();

        BigDecimal dailyProfitAmount =
                currentTotalAsset.subtract(previousTotalAsset);

        BigDecimal dailyProfitRate = BigDecimal.ZERO;

        if (previousTotalAsset.compareTo(BigDecimal.ZERO) > 0) {
            dailyProfitRate = dailyProfitAmount
                    .divide(previousTotalAsset, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        BigDecimal baseProfitAmount =
                currentTotalAsset.subtract(BASE_CAPITAL);

        BigDecimal baseProfitRate = BigDecimal.ZERO;

        if (BASE_CAPITAL.compareTo(BigDecimal.ZERO) > 0) {
            baseProfitRate = baseProfitAmount
                    .divide(BASE_CAPITAL, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        accountSummary.setDailyProfitAmount(dailyProfitAmount);
        accountSummary.setDailyProfitRate(dailyProfitRate);

        accountSummary.setBaseCapital(BASE_CAPITAL);
        accountSummary.setBaseProfitAmount(baseProfitAmount);
        accountSummary.setBaseProfitRate(baseProfitRate);

        return accountSummary;
    }
    
    
}
