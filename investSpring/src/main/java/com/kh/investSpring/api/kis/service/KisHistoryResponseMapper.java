package com.kh.investSpring.api.kis.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class KisHistoryResponseMapper {

    private KisHistoryResponseMapper() {
    }

    static List<Map<String, Object>> extractHistoryRows(Map<String, Object> body) {
        if (body == null) {
            return List.of();
        }

        List<Map<String, Object>> fromOutput2 = castOutputRows(body.get("output2"));

        if (!fromOutput2.isEmpty()) {
            return fromOutput2;
        }

        return castOutputRows(body.get("output"));
    }

    private static List<Map<String, Object>> castOutputRows(Object raw) {
        if (!(raw instanceof List<?> rawList)) {
            return List.of();
        }

        List<Map<String, Object>> list = new ArrayList<>();

        for (Object item : rawList) {
            if (!(item instanceof Map<?, ?> map)) {
                continue;
            }

            if (!map.containsKey("stck_bsop_date")) {
                continue;
            }

            Map<String, Object> cast = new LinkedHashMap<>();

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                cast.put(String.valueOf(entry.getKey()), entry.getValue());
            }

            list.add(cast);
        }

        return list;
    }
}
