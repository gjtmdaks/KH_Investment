package com.kh.investSpring.api.dart.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.investSpring.api.dart.config.DartProperties;
import com.kh.investSpring.api.dart.dao.corpInformationDao;
import com.kh.investSpring.api.dart.dto.StockTotalQuantityDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DartStockService {

	private final DartProperties properties;
	private final HttpClient httpClient;
    private final corpInformationDao coDao;
    private final ObjectMapper objectMapper;

    @Transactional
    public void syncStockTotals() {

        List<Map<String, Object>> corpList = coDao.selectCorpCodes();
        int year = LocalDate.now().getYear()-1;

        for (Map<String, Object> corp : corpList) {
            try {
                String corpCode = String.valueOf(corp.get("CORP_CODE"));
                String stockCode = String.valueOf(corp.get("STOCK_CODE"));

                StockTotalQuantityDto dto = requestStockTotals(
					                                corpCode,
					                                stockCode,
					                                String.valueOf(year),
					                                "11011"
					                        );
                if (dto != null) {
                    coDao.updateStockTotals(dto);
                }
                Thread.sleep(120);

            } catch (Exception e) {
                log.error("주식 총수 저장 실패 stockCode={}",
                        corp.get("STOCK_CODE"), e
                );
            }
        }
        log.info("주식 총수 저장 완료");
    }

    public StockTotalQuantityDto requestStockTotals(
            String corpCode,
            String stockCode,
            String year,
            String reportCode
    ) throws IOException, InterruptedException {

    	String url =
    	        "https://opendart.fss.or.kr/api/stockTotqySttus.json"
    	        + "?crtfc_key=" + properties.getAppKey()
    	        + "&corp_code=" + corpCode
    	        + "&bsns_year=" + year
    	        + "&reprt_code=" + reportCode;

    	HttpRequest request = HttpRequest.newBuilder()
	    	                .uri(URI.create(url))
	    	                .GET()
	    	                .build();

    	HttpResponse<String> response = httpClient.send(
						    	                request,
						    	                HttpResponse.BodyHandlers.ofString()
						    	        );
    	if (response.statusCode() != 200) {
    	    return null;
    	}

    	Map<String, Object> body = objectMapper.readValue(
					    	                response.body(),
					    	                new TypeReference<Map<String, Object>>() {}
					    	        );
    	if (body == null) {
    	    return null;
    	}

    	if (!"000".equals(body.get("status"))) {
    	    log.warn("발행주식수 응답 실패 corpCode={} status={} message={}",
    	            corpCode,
    	            body.get("status"),
    	            body.get("message")
    	    );
    	    return null;
    	}

    	List<Map<String, Object>> list = (List<Map<String, Object>>) body.get("list");

        if (list == null || list.isEmpty()) {
            return null;
        }

        Map<String, Object> target = null;

        // "합계" row 우선 사용
        for (Map<String, Object> item : list) {

            String se = String.valueOf(item.get("se"));

            if ("합계".equals(se)) {
                target = item;
                break;
            }
        }

        if (target == null) {
            target = list.get(0);
        }

        StockTotalQuantityDto dto = new StockTotalQuantityDto();

        dto.setCorpCode(corpCode);
        dto.setStockCode(stockCode);
        dto.setIssuedStock(
                parseLong(target.get("now_to_isu_stock_totqy"))
        );
        dto.setDeclinedStock(
                parseLong(target.get("now_to_dcrs_stock_totqy"))
        );
        dto.setTreasuryStock(
                parseLong(target.get("tesstk_co"))
        );
        dto.setOutstandingShares(
                parseLong(target.get("distb_stock_co"))
        );

        return dto;
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }

        String str = String.valueOf(value)
                    .replace(",", "")
                    .replace("-", "0")
                    .trim();

        if (str.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(str);
        } catch (Exception e) {
            return null;
        }
    }
}