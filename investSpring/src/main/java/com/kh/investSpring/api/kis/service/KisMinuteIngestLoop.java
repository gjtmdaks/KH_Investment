package com.kh.investSpring.api.kis.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import com.kh.investSpring.api.kis.dto.StockIntradayMinuteCacheDto;

final class KisMinuteIngestLoop {

    private KisMinuteIngestLoop() {
    }

    @FunctionalInterface
    interface BatchFetcher {
        List<Map<String, Object>> fetch(String cursorHhmmss)
                throws IOException, InterruptedException;
    }

    static List<StockIntradayMinuteCacheDto> collectTradingDay(
            String stockCode,
            java.time.LocalDate tradeDate,
            String initialCursor,
            BatchFetcher batchFetcher
    ) throws IOException, InterruptedException {
        TreeMap<String, StockIntradayMinuteCacheDto> merged = new TreeMap<>();

        String cursor = initialCursor;
        int iterations = 0;

        while (iterations++ < 96) {
            List<Map<String, Object>> batch = batchFetcher.fetch(cursor);

            if (batch.isEmpty()) {
                String prev = KisMinuteBarMapper.minusOneMinute(cursor);

                if (prev.compareTo("085959") <= 0) {
                    break;
                }

                cursor = prev;
                Thread.sleep(280);

                continue;
            }

            int sizeBefore = merged.size();

            for (Map<String, Object> row : batch) {
                StockIntradayMinuteCacheDto dto = KisMinuteBarMapper.toDto(stockCode, row);

                if (dto == null || dto.getTradeDate() == null || dto.getBarTime() == null) {
                    continue;
                }

                if (!dto.getTradeDate().equals(tradeDate)) {
                    continue;
                }

                merged.put(dto.getBarTime(), dto);
            }

            String minTime = batch.stream()
                    .map(row -> KisMinuteBarMapper.normalizeHhmmss(KisMinuteBarMapper.readCntgHour(row)))
                    .filter(s -> s != null && s.length() == 6)
                    .min(Comparator.naturalOrder())
                    .orElse(null);

            if (minTime == null) {
                break;
            }

            if (merged.size() == sizeBefore) {
                String prev = KisMinuteBarMapper.minusOneMinute(cursor);

                if (prev.compareTo(minTime) >= 0) {
                    cursor = prev;
                } else {
                    cursor = KisMinuteBarMapper.minusOneMinute(minTime);
                }
            } else {
                cursor = KisMinuteBarMapper.minusOneMinute(minTime);
            }

            if (minTime.compareTo("090000") <= 0) {
                break;
            }

            Thread.sleep(320);
        }

        List<StockIntradayMinuteCacheDto> ordered = new ArrayList<>(merged.values());
        ordered.sort(Comparator.comparing(StockIntradayMinuteCacheDto::getBarTime));

        return ordered;
    }
}
