package com.kh.investSpring.api.kis.websocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.kh.investSpring.api.kis.dto.KisStockOrderbookResponse;
import com.kh.investSpring.api.kis.dto.KisStockOrderbookResponse.OrderbookLevel;

final class KisOrderbookPayloadMapper {

    private static final String TR_PREFIX = "0|H0STASP0";
    private static final int MIN_FIELD_COUNT = 59;

    private KisOrderbookPayloadMapper() {
    }

    static Optional<KisStockOrderbookResponse> parse(String payload) {
        if (payload == null || !payload.startsWith(TR_PREFIX)) {
            return Optional.empty();
        }

        String[] split = payload.split("\\|", -1);
        if (split.length < 4) {
            return Optional.empty();
        }

        int count;
        try {
            count = Integer.parseInt(split[2].trim());
        } catch (NumberFormatException e) {
            return Optional.empty();
        }

        String body = split[3];
        String[] data = body.split("\\^", -1);

        for (int i = 0; i < count; i++) {
            int start = i * MIN_FIELD_COUNT;
            if (data.length < start + MIN_FIELD_COUNT) {
                break;
            }

            String stockCode = trimToNull(data[start]);
            if (stockCode == null || stockCode.isBlank()) {
                continue;
            }

            List<OrderbookLevel> asks = new ArrayList<>();
            for (int level = 1; level <= 10; level++) {
                asks.add(new OrderbookLevel(
                        level,
                        trimToNull(data[start + 2 + level]),
                        trimToNull(data[start + 22 + level]),
                        null));
            }

            List<OrderbookLevel> bids = new ArrayList<>();
            for (int level = 1; level <= 10; level++) {
                bids.add(new OrderbookLevel(
                        level,
                        trimToNull(data[start + 12 + level]),
                        trimToNull(data[start + 32 + level]),
                        null));
            }

            return Optional.of(new KisStockOrderbookResponse(
                    stockCode,
                    asks,
                    bids,
                    trimToNull(data[start + 43]),
                    trimToNull(data[start + 44]),
                    trimToNull(data[start + 47]),
                    trimToNull(data[start + 48])));
        }

        return Optional.empty();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
