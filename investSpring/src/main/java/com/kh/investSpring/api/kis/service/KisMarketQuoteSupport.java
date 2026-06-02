package com.kh.investSpring.api.kis.service;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Component;

import com.kh.investSpring.api.kis.config.KisProperties;
import com.kh.investSpring.api.kis.market.KisMarketDivCode;
import com.kh.investSpring.api.kis.market.MarketSessionResolver;
import com.kh.investSpring.api.kis.market.QuoteSession;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KisMarketQuoteSupport {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final KisProperties kisProperties;
    private final MarketSessionResolver marketSessionResolver;

    public QuoteSession currentSession() {
        return marketSessionResolver.resolve();
    }

    public KisMarketDivCode marketDivCode(QuoteSession session) {
        if (!kisProperties.isNxtQuoteEnabled()) {
            return KisMarketDivCode.KRX;
        }

        if (session == QuoteSession.NXT_PRE || session == QuoteSession.NXT_AFTER) {
            return KisMarketDivCode.NXT;
        }

        return KisMarketDivCode.KRX;
    }

    public String cacheSuffix(QuoteSession session, KisMarketDivCode marketDivCode) {
        return marketDivCode.code() + ":" + session.name();
    }

    public String asOfNow() {
        return ZonedDateTime.now(SEOUL).toOffsetDateTime().toString();
    }

    public boolean isStale(QuoteSession session) {
        return session == QuoteSession.CLOSED;
    }

    public boolean acceptsRealtimeSource(String quoteSource) {
        return marketSessionResolver.acceptsRealtimeSource(currentSession(), quoteSource);
    }
}
