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
import com.kh.investSpring.api.dart.dto.MinorityShareholderDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DartMinorityShareholderService {

    private final DartProperties properties;
    private final HttpClient httpClient;
    private final corpInformationDao coDao;
    private final ObjectMapper objectMapper;

    @Transactional
    public void syncMinorityShareholders() {

        List<Map<String, Object>> corpList = coDao.selectCorpCodes();

        int year = LocalDate.now().getYear() - 1;

        for (Map<String, Object> corp : corpList) {
            try {
                String corpCode = String.valueOf(corp.get("CORP_CODE"));
                String stockCode = String.valueOf(corp.get("STOCK_CODE"));
                MinorityShareholderDto dto = requestMinorityShareholder(
					                                corpCode,
					                                stockCode,
					                                String.valueOf(year),
					                                "11011"
					                        );

                if (dto != null) {
                	log.info(
                		    "stockCode={} shareholderRatio={} ownershipRatio={}",
                		    dto.getStockCode(),
                		    dto.getMinorityShareholderRatio(),
                		    dto.getMinorityOwnershipRatio()
                		);
                    coDao.updateMinorityShareholder(dto);
                }

                Thread.sleep(120);

            } catch (Exception e) {

                log.error(
                        "소액주주 저장 실패 stockCode={}",
                        corp.get("STOCK_CODE"),
                        e
                );
            }
        }

        log.info("소액주주 저장 완료");
    }

    public MinorityShareholderDto requestMinorityShareholder(
            String corpCode,
            String stockCode,
            String year,
            String reportCode
    ) throws IOException, InterruptedException {

        String url =
                "https://opendart.fss.or.kr/api/mrhlSttus.json"
                + "?crtfc_key=" + properties.getAppKey()
                + "&corp_code=" + corpCode
                + "&bsns_year=" + year
                + "&reprt_code=" + reportCode;

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() != 200) {
            return null;
        }

        Map<String, Object> body =
                objectMapper.readValue(
                        response.body(),
                        new TypeReference<Map<String, Object>>() {}
                );

        if (body == null) {
            return null;
        }

        if (!"000".equals(body.get("status"))) {

            log.warn(
                    "소액주주 응답 실패 corpCode={} status={}",
                    corpCode,
                    body.get("status")
            );

            return null;
        }

        List<Map<String, Object>> list =
                (List<Map<String, Object>>) body.get("list");

        if (list == null || list.isEmpty()) {
            return null;
        }

        Map<String, Object> target = list.get(0);

        MinorityShareholderDto dto =
                new MinorityShareholderDto();

        dto.setCorpCode(corpCode);
        dto.setStockCode(stockCode);

        dto.setMinorityShareholderRatio(
                parseDouble(target.get("shrholdr_rate"))
        );

        dto.setMinorityOwnershipRatio(
                parseDouble(target.get("hold_stock_rate"))
        );

        return dto;
    }

    private Double parseDouble(Object value) {

        if (value == null) {
            return 0.0;
        }

        String str = String.valueOf(value)
                .replace(",", "")
                .replace("%", "")
                .trim();

        if (str.isBlank() || "-".equals(str)) {
            return 0.0;
        }

        try {

            double result = Double.parseDouble(str);

            // 비정상 값 방어
            if (result < 0 || result > 100) {

                log.warn("비정상 비율 데이터 value={}", result);

                return 0.0;
            }

            return result;

        } catch (Exception e) {
            return 0.0;
        }
    }
}