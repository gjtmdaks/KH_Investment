package com.kh.investSpring.api.kis.service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.kh.investSpring.api.kis.dto.KisStockOrderbookResponse;

@Service
public class KisOrderbookRealtimeCache {

    private final ConcurrentHashMap<String, CachedEntry> cache = new ConcurrentHashMap<>();

    public void put(KisStockOrderbookResponse response) {
        if (response == null || response.stockCode() == null) {
            return;
        }
        String key = normalizeKey(response.stockCode());
        if (key.isBlank()) {
            return;
        }
        cache.put(key, new CachedEntry(response, System.currentTimeMillis()));
    }

    public Optional<KisStockOrderbookResponse> getFresh(String stockCode, long ttlMs) {
        String key = normalizeKey(stockCode);
        if (key.isBlank()) {
            return Optional.empty();
        }

        CachedEntry entry = cache.get(key);
        if (entry == null) {
            return Optional.empty();
        }

        long ageMs = System.currentTimeMillis() - entry.updatedAtMs();
        if (ageMs < 0L || ageMs > ttlMs) {
            return Optional.empty();
        }

        return Optional.of(entry.value());
    }

    public void evict(String stockCode) {
        String key = normalizeKey(stockCode);
        if (!key.isBlank()) {
            cache.remove(key);
        }
    }

    private static String normalizeKey(String stockCode) {
        return stockCode == null ? "" : stockCode.trim();
    }

    private record CachedEntry(KisStockOrderbookResponse value, long updatedAtMs) {
    }
}
