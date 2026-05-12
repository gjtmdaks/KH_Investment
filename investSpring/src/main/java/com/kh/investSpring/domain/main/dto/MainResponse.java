package com.kh.investSpring.domain.main.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MainResponse {

    private Header header;
    private Sidebar sidebar;
    private Main main;

    @Data
    @Builder
    public static class Header {
        private Long userNo;
        private String userName;
    }

    @Data
    @Builder
    public static class Sidebar {
        private Account account;
        private List<Holding> holdings;
        private List<Watchlist> watchlist;
        private List<RecentView> recentView;
    }

    @Data
    @Builder
    public static class Account {
        private Long balance;
    }

    @Data
    @Builder
    public static class Holding {
        private String stockCode;
        private String stockName;
        private Long quantity;
        private Long avgPrice;
        private Long currentPrice;
    }

    @Data
    @Builder
    public static class Watchlist {
        private String stockCode;
        private String stockName;
        private Long price;
        private Double changeRate;
    }

    @Data
    @Builder
    public static class RecentView {
        private String stockCode;
        private String stockName;
    }

    @Data
    @Builder
    public static class Main {
        private TopStock topVolumeStock;
        private List<Stock> stockList;
    }

    @Data
    @Builder
    public static class TopStock {
        private String stockCode;
        private String stockName;
        private Long price;
        private Double changeRate;
        private List<Long> miniChart;
        private List<CommunityPreview> communityPreview;
    }

    @Data
    @Builder
    public static class CommunityPreview {
        private Long boardNo;
        private String content;
        private Long likeCount;
    }

    @Data
    @Builder
    public static class Stock {
        private String stockCode;
        private String stockName;
        private Long price;
        private Double changeRate;
        private Long volume;
        private Long tradingValue;
    }
}