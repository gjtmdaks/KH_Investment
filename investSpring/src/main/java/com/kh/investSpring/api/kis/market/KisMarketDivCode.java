package com.kh.investSpring.api.kis.market;

public enum KisMarketDivCode {
    KRX("J"),
    NXT("NX"),
    UNIFIED("UN");

    private final String code;

    KisMarketDivCode(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
