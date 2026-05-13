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

    @Override
    public AccountSummaryDto getAccountSummary(Long userNo) {
        AccountSummaryDto accountSummary = accountDao.selectAccountSummaryByUserNo(userNo);

        if (accountSummary == null) {
            return null;
        }

        BigDecimal currentTotalAsset = accountSummary.getCurrentTotalAsset();
        BigDecimal initialBalance = accountSummary.getInitialBalance();

        BigDecimal profitAmount = currentTotalAsset.subtract(initialBalance);

        BigDecimal profitRate = BigDecimal.ZERO;

        if (initialBalance.compareTo(BigDecimal.ZERO) > 0) {
            profitRate = profitAmount
                    .divide(initialBalance, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        accountSummary.setProfitAmount(profitAmount);
        accountSummary.setProfitRate(profitRate);
        accountSummary.setInitialProfitRate(profitRate);

        return accountSummary;
    }
}
