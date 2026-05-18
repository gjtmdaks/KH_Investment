package com.kh.investSpring.domain.notice.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NoticePageResponse {

    private List<NoticeListResponse> notices;
    private int currentPage;
    private int totalPage;
    private long totalCount;
}