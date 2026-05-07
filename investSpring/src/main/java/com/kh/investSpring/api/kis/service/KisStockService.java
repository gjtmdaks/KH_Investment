package com.kh.investSpring.api.kis.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.kh.investSpring.api.kis.config.KisProperties;
import com.kh.investSpring.api.kis.dto.KisStockPriceResponse;

@Service
public class KisStockService {

	private final RestClient restClient;
    private final KisProperties kisProperties;
    private final KisTokenService kisTokenService;

    public KisStockService(
            RestClient restClient,
            KisProperties kisProperties,
            KisTokenService kisTokenService
    ) {
        this.restClient = restClient;
        this.kisProperties = kisProperties;
        this.kisTokenService = kisTokenService;
    }

    public KisStockPriceResponse getStockPrice(String stockCode) {
        String accessToken = kisTokenService.getAccessToken();

        String url = kisProperties.getBaseUrl()
                + "/uapi/domestic-stock/v1/quotations/inquire-price"
                + "?FID_COND_MRKT_DIV_CODE=J"
                + "&FID_INPUT_ISCD=" + stockCode;

        KisInquirePriceResponse response = restClient.get()
                .uri(url)
                .header("content-type", "application/json; charset=utf-8")
                .header("authorization", "Bearer " + accessToken)
                .header("appkey", kisProperties.getAppKey())
                .header("appsecret", kisProperties.getAppSecret())
                .header("tr_id", "FHKST01010100")
                .retrieve()
                .body(KisInquirePriceResponse.class);

        if (response == null) {
            throw new IllegalStateException("한국투자증권 현재가 응답이 없습니다.");
        }

        if (!"0".equals(response.rt_cd())) {
            throw new IllegalStateException("한국투자증권 현재가 조회 실패: " + response.msg1());
        }

        Map<String, Object> output = response.output();

        return new KisStockPriceResponse(
                stockCode,
                valueToString(output.get("hts_kor_isnm")),
                valueToString(output.get("stck_prpr")),
                valueToString(output.get("prdy_vrss")),
                valueToString(output.get("prdy_ctrt")),
                valueToString(output.get("acml_vol")),
                valueToString(output.get("acml_tr_pbmn")),
                valueToString(output.get("stck_oprc")),
                valueToString(output.get("stck_hgpr")),
                valueToString(output.get("stck_lwpr"))
        );
    }

    private String valueToString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private record KisInquirePriceResponse(
            String rt_cd,
            String msg_cd,
            String msg1,
            Map<String, Object> output
    ) {
    }

}
