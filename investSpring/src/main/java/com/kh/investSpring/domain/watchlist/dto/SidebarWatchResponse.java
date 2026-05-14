package com.kh.investSpring.domain.watchlist.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SidebarWatchResponse {

    private boolean loggedIn;
    private boolean hasWatchlist;

    // 실제 관심종목 코드 목록
    private List<String> watchlistCodes;

    // 화면에 표시할 목록
    private List<SidebarWatchDto> stockList;
}