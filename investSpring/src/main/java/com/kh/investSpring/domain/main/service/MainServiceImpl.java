package com.kh.investSpring.domain.main.service;

import org.springframework.stereotype.Service;

import com.kh.investSpring.domain.account.service.AccountService;
import com.kh.investSpring.domain.main.dto.MainResponse;
import com.kh.investSpring.domain.stock.service.StockService;
import com.kh.investSpring.domain.user.service.UserService;
import com.kh.investSpring.domain.watchlist.service.WatchlistService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MainServiceImpl implements MainService {

    private final UserService userService;
    private final AccountService accountService;
    private final StockService stockService;
    private final WatchlistService watchlistService;

    @Override
    public MainResponse getMain(Long userNo) {

        return MainResponse.builder()
                .header(buildHeader(userNo))
                .sidebar(buildSidebar(userNo))
                .main(buildMain())
                .build();
    }

    // ✅ Header
    private MainResponse.Header buildHeader(Long userNo) {

        var user = userService.getUser(userNo);

        return MainResponse.Header.builder()
                .userNo(user.getUserNo())
                .userName(user.getUserName())
                .build();
    }

    // ✅ Sidebar
    private MainResponse.Sidebar buildSidebar(Long userNo) {

        var account = accountService.getAccount(userNo);
        var holdings = accountService.getHoldings(userNo);

        return MainResponse.Sidebar.builder()
                .account(
                        MainResponse.Account.builder()
                                .balance(account.getBalance())
                                .build()
                )
                .holdings(
                        holdings.stream()
                                .map(h -> MainResponse.Holding.builder()
                                        .stockCode(h.getStockCode())
                                        .stockName(h.getStockName())
                                        .quantity(h.getQuantity())
                                        .avgPrice(h.getAvgPrice())
                                        .currentPrice(h.getCurrentPrice())
                                        .build()
                                ).toList()
                )
                .build();
    }

    // ✅ Main 영역
    private MainResponse.Main buildMain() {

        var stocks = stockService.getStockList();
        var top = stockService.getTopVolumeStock();

        return MainResponse.Main.builder()
                .stockList(
                        stocks.stream()
                                .map(s -> MainResponse.Stock.builder()
                                        .stockCode(s.getStockCode())
                                        .stockName(s.getStockName())
                                        .price(s.getPrice())
                                        .changeRate(s.getChangeRate())
                                        .volume(s.getVolume())
                                        .build()
                                ).toList()
                )
                .topVolumeStock(
                        MainResponse.TopStock.builder()
                                .stockCode(top.getStockCode())
                                .stockName(top.getStockName())
                                .price(top.getPrice())
                                .changeRate(top.getChangeRate())
                                .miniChart(top.getMiniChart())
                                .communityPreview(
                                        top.getCommunity().stream()
                                                .map(c -> MainResponse.CommunityPreview.builder()
                                                        .boardNo(c.getBoardNo())
                                                        .content(c.getContent())
                                                        .likeCount(c.getLikeCount())
                                                        .build()
                                                ).toList()
                                )
                                .build()
                )
                .build();
    }
}