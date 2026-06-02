package com.kh.investSpring.domain.account.service;

import org.springframework.stereotype.Service;

import com.kh.investSpring.api.kis.service.KisSidebarQuoteEnricher;
import com.kh.investSpring.domain.account.dao.AccountDao;
import com.kh.investSpring.domain.account.dto.AccountAssetResponse;
import com.kh.investSpring.domain.account.dto.AccountAssetSummaryDto;
import com.kh.investSpring.domain.account.dto.AccountSummaryDto;
import com.kh.investSpring.domain.account.dto.AccountTradeStatusResponse;
import com.kh.investSpring.domain.account.dto.RankingResponse;
import com.kh.investSpring.domain.account.dto.RankingResponse.RankingType;
import com.kh.investSpring.domain.main.dto.MainResponse.Account;
import com.kh.investSpring.domain.main.dto.MainResponse.Holding;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private static final long RANKING_CACHE_TTL_MS = 10_000L;
    private static final long ACCOUNT_ASSETS_CACHE_TTL_MS = 2_000L;

    private final AccountDao accountDao;
    private final KisSidebarQuoteEnricher kisSidebarQuoteEnricher;
    private final Map<RankingType, CachedRanking> rankingCache = new ConcurrentHashMap<>();
    private final Map<Long, CachedAccountAssets> accountAssetsCache = new ConcurrentHashMap<>();
    private static final BigDecimal BASE_CAPITAL = BigDecimal.valueOf(10000000); // 기본자산은 고정
    
    // 전일(장시작) 돈 자동저장
    @Override
	public int updatePreviousTotalAssetForAllActiveAccounts() {
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

    @Override
    public AccountAssetResponse getAccountAssets(Long userNo) {
        if (userNo == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        long now = System.currentTimeMillis();
        CachedAccountAssets cached = accountAssetsCache.get(userNo);

        if (cached != null && cached.expiresAtMs() > now) {
            return cached.assets();
        }

        AccountAssetResponse response = loadAccountAssets(userNo);
        accountAssetsCache.put(
                userNo,
                new CachedAccountAssets(response, now + ACCOUNT_ASSETS_CACHE_TTL_MS)
        );

        return response;
    }

    private AccountAssetResponse loadAccountAssets(Long userNo) {
        AccountAssetSummaryDto asset =
                accountDao.selectAccountAssetByUserNo(userNo);

        if (asset == null) {
            return AccountAssetResponse.builder()
                    .totalAsset(BigDecimal.ZERO)
                    .availableCash(BigDecimal.ZERO)
                    .lockedCash(BigDecimal.ZERO)
                    .totalStockValue(BigDecimal.ZERO)
                    .holdings(List.of())
                    .build();
        }

        List<AccountAssetResponse.HoldingStock> holdings =
                accountDao.selectHoldingStocksByUserNo(userNo).stream()
                        .map(kisSidebarQuoteEnricher::enrichHolding)
                        .toList();

        return AccountAssetResponse.builder()
                .totalAsset(asset.getTotalAsset())
                .availableCash(asset.getAvailableCash())
                .lockedCash(asset.getLockedCash())
                .totalStockValue(asset.getTotalStockValue())
                .holdings(holdings != null ? holdings : List.of())
                .build();
    }

    @Override
    public Account getSidebarAccount(Long userNo) {
        if (userNo == null) {
            return null;
        }

        return accountDao.selectSidebarAccountByUserNo(userNo);
    }

    @Override
    public List<Holding> getSidebarHoldings(Long userNo) {
        if (userNo == null) {
            return List.of();
        }

        List<Holding> holdings = accountDao.selectSidebarHoldingsByUserNo(userNo);

        return holdings != null ? holdings : List.of();
    }

    @Override
    public void validateAccountCanTrade(Long userNo) {
        AccountTradeStatusResponse account =
                accountDao.selectAccountTradeStatusByUserNo(userNo);

        if (account == null) {
            throw new IllegalStateException("계좌가 없습니다.");
        }

        if ("CLOSE".equals(account.getStatus())) {
            throw new IllegalStateException("폐쇄된 계좌입니다.");
        }

        if ("STOP".equals(account.getStatus())) {
            throw new IllegalStateException("거래가 정지된 계좌입니다.");
        }
    }

	@Override
	public List<RankingResponse> getRanking(RankingType type) {
        long now = System.currentTimeMillis();
        CachedRanking cached = rankingCache.get(type);

        if (cached != null && cached.expiresAtMs() > now) {
            return cached.rankings();
        }

        List<RankingResponse> rankings = accountDao.getRanking(type);
        List<RankingResponse> safeRankings =
                rankings != null ? List.copyOf(rankings) : List.of();

        rankingCache.put(
                type,
                new CachedRanking(safeRankings, now + RANKING_CACHE_TTL_MS)
        );

		return safeRankings;
	}

    private record CachedRanking(
            List<RankingResponse> rankings,
            long expiresAtMs
    ) {
    }

    private record CachedAccountAssets(
            AccountAssetResponse assets,
            long expiresAtMs
    ) {
    }
    
}
