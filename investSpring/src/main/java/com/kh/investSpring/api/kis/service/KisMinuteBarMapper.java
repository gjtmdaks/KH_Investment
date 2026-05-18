package com.kh.investSpring.api.kis.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.kh.investSpring.api.kis.dto.StockIntradayMinuteCacheDto;

final class KisMinuteBarMapper {

    static final DateTimeFormatter BASIC_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    private KisMinuteBarMapper() {
    }

    static Object extractOutput2(Map<String, Object> body) {
        Object out2 = body.get("output2");

        if (out2 != null) {
            return out2;
        }

        Object output = body.get("output");

        if (output instanceof List<?> list && !list.isEmpty()) {
            Object first = list.get(0);

            if (first instanceof Map<?, ?> row && row.containsKey("stck_cntg_hour")) {
                return output;
            }
        }

        return null;
    }

    static List<Map<String, Object>> castOutputRows(Object out2) {
        if (!(out2 instanceof List<?> rawList)) {
            return List.of();
        }

        List<Map<String, Object>> list = new ArrayList<>();

        for (Object item : rawList) {
            if (item instanceof Map<?, ?> m) {
                Map<String, Object> cast = new LinkedHashMap<>();

                for (Map.Entry<?, ?> e : m.entrySet()) {
                    cast.put(String.valueOf(e.getKey()), e.getValue());
                }

                list.add(cast);
            }
        }

        return list;
    }

    static StockIntradayMinuteCacheDto toDto(String stockCode, Map<String, Object> item) {
        LocalDate tradeDate = parseTradeDate(item.get("stck_bsop_date"));

        if (tradeDate == null) {
            return null;
        }

        String barTime = normalizeHhmmss(readCntgHour(item));

        if (barTime == null) {
            return null;
        }

        StockIntradayMinuteCacheDto dto = new StockIntradayMinuteCacheDto();
        dto.setStockCode(stockCode);
        dto.setTradeDate(tradeDate);
        dto.setBarTime(barTime);
        dto.setOpenPrice(parseLong(item.get("stck_oprc")));
        dto.setHighPrice(parseLong(item.get("stck_hgpr")));
        dto.setLowPrice(parseLong(item.get("stck_lwpr")));
        dto.setClosePrice(
                firstPositiveLong(
                        item.get("stck_prpr"),
                        item.get("stck_clpr")
                )
        );
        dto.setVolume(
                firstPositiveLong(
                        item.get("cntg_vol"),
                        item.get("acml_vol")
                )
        );

        return dto;
    }

    static String readCntgHour(Map<String, Object> item) {
        Object v = item.get("stck_cntg_hour");

        return v == null ? null : String.valueOf(v).trim();
    }

    static LocalDate parseTradeDate(Object raw) {
        if (raw == null) {
            return null;
        }

        String s = String.valueOf(raw).trim();

        if (s.length() != 8 || !s.chars().allMatch(Character::isDigit)) {
            return null;
        }

        return LocalDate.parse(s, BASIC_DATE);
    }

    static String normalizeHhmmss(String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }

        String digits = raw.replace(":", "").trim();

        if (digits.length() >= 6) {
            return digits.substring(digits.length() - 6);
        }

        return "0".repeat(6 - digits.length()) + digits;
    }

    static String minusOneMinute(String hhmmss) {
        int h = Integer.parseInt(hhmmss.substring(0, 2));
        int m = Integer.parseInt(hhmmss.substring(2, 4));
        int s = Integer.parseInt(hhmmss.substring(4, 6));
        LocalTime t = LocalTime.of(h, m, s).minusMinutes(1);

        return String.format("%02d%02d%02d", t.getHour(), t.getMinute(), t.getSecond());
    }

    private static long parseLong(Object v) {
        if (v == null) {
            return 0L;
        }

        try {
            return Long.parseLong(
                    String.valueOf(v)
                            .replace(",", "")
                            .trim()
            );
        } catch (Exception e) {
            return 0L;
        }
    }

    private static long firstPositiveLong(Object primary, Object secondary) {
        long a = parseLong(primary);

        if (a > 0L) {
            return a;
        }

        return parseLong(secondary);
    }
}
