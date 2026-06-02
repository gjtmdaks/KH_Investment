package com.kh.investSpring.api.kis.market;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Component;

import com.kh.investSpring.api.kis.config.KisProperties;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MarketSessionResolver {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final KisProperties kisProperties;

    public QuoteSession resolve() {
        return resolve(ZonedDateTime.now(SEOUL).toLocalTime());
    }

    public QuoteSession resolve(LocalTime now) {
        LocalTime nxtPreStart = parseTime(kisProperties.getNxtPreStartTime(), LocalTime.of(8, 0));
        LocalTime krxRegularStart = parseTime(kisProperties.getKrxRegularStartTime(), LocalTime.of(9, 0));
        LocalTime krxRegularEnd = parseTime(kisProperties.getKrxRegularEndTime(), LocalTime.of(15, 30));
        LocalTime nxtAfterEnd = parseTime(kisProperties.getNxtAfterEndTime(), LocalTime.of(20, 0));

        if (isOnOrAfter(now, nxtPreStart) && now.isBefore(krxRegularStart)) {
            return QuoteSession.NXT_PRE;
        }

        if (isOnOrAfter(now, krxRegularStart) && !now.isAfter(krxRegularEnd)) {
            return QuoteSession.KRX_REGULAR;
        }

        if (now.isAfter(krxRegularEnd) && !now.isAfter(nxtAfterEnd)) {
            return QuoteSession.NXT_AFTER;
        }

        return QuoteSession.CLOSED;
    }

    public boolean acceptsRealtimeSource(QuoteSession session, String quoteSource) {
        if (quoteSource == null || quoteSource.isBlank()) {
            return true;
        }

        String normalized = quoteSource.trim().toUpperCase();
        if (normalized.startsWith("H0UN")) {
            return session != QuoteSession.CLOSED;
        }

        if (normalized.startsWith("H0ST")) {
            return session == QuoteSession.KRX_REGULAR;
        }

        if (normalized.startsWith("H0NX")) {
            return session == QuoteSession.NXT_PRE || session == QuoteSession.NXT_AFTER;
        }

        return true;
    }

    private static boolean isOnOrAfter(LocalTime value, LocalTime floor) {
        return value.equals(floor) || value.isAfter(floor);
    }

    private static LocalTime parseTime(String value, LocalTime fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return LocalTime.parse(value.trim());
    }
}
