package com.kh.investSpring.domain.main.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kh.investSpring.domain.account.service.AccountService;
import com.kh.investSpring.domain.main.dto.MainResponse;
import com.kh.investSpring.domain.stock.dto.StockDto;
import com.kh.investSpring.domain.stock.dto.TopStockDto;
import com.kh.investSpring.domain.stock.service.StockService;
import com.kh.investSpring.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MainServiceImpl implements MainService {

    private final UserService userService;
    private final AccountService accountService;
    private final StockService stockService;

    @Override
    public MainResponse getMain(Long userNo) {

        // 로그인 안 된 경우
        if (userNo == null) {
            return MainResponse.builder()
                    .header(null)
                    .sidebar(null)
                    .main(buildMain())
                    .build();
        }

        // 로그인 된 경우
        return MainResponse.builder()
                .header(userService.getHeader(userNo))
                .sidebar(buildSidebar(userNo))
                .main(buildMain())
                .build();
    }

    // ✅ Sidebar
    private MainResponse.Sidebar buildSidebar(Long userNo) {

        if (userNo == null) {
            return MainResponse.Sidebar.builder()
                    .account(null)
                    .holdings(List.of())
                    .watchlist(List.of())
                    .recentView(List.of())
                    .build();
        }

        // 로그인 상태
        return MainResponse.Sidebar.builder()
                .account(null)
                .holdings(List.of())
                .watchlist(List.of())
                .recentView(List.of())
                .build();
    }

    // ✅ Main 영역
    private MainResponse.Main buildMain() {

        List<StockDto> stocks;
        TopStockDto top;

        try {
            stocks = stockService.getStockList();
        } catch (Exception e) {
            stocks = List.of();
        }

        try {
            top = stockService.getTopVolumeStock();
        } catch (Exception e) {
            top = null;
        }

        // 🔹 stocks 처리
        List<MainResponse.Stock> stockList =
                (stocks == null || stocks.isEmpty())
                        ? List.of()
                        : stocks.stream()
                        .map(s -> MainResponse.Stock.builder()
                                .stockCode(s.getStockCode())
                                .stockName(s.getStockName())
                                .price(s.getPrice())
                                .changeRate(s.getChangeRate())
                                .volume(s.getVolume())
                                .build()
                        ).toList();

        // 🔹 top 처리
        MainResponse.TopStock topStock = null;

        if (top != null) {
            try {
                topStock = MainResponse.TopStock.builder()
                        .stockCode(top.getStockCode())
                        .stockName(top.getStockName())
                        .price(top.getPrice())
                        .changeRate(top.getChangeRate())
                        .miniChart(
                                top.getMiniChart() != null
                                        ? top.getMiniChart()
                                        : List.of()
                        )
                        .communityPreview(
                                top.getCommunity() != null
                                        ? top.getCommunity().stream()
                                        .map(c -> MainResponse.CommunityPreview.builder()
                                                .boardNo(c.getBoardNo())
                                                .content(c.getContent())
                                                .likeCount(c.getLikeCount())
                                                .build()
                                        ).toList()
                                        : List.of()
                        )
                        .build();
            } catch (Exception e) {
                topStock = null;
            }
        }

        return MainResponse.Main.builder()
                .stockList(stockList)
                .topVolumeStock(topStock)
                .build();
    }
}