"use client";

import { useCallback, useEffect, useRef, useState } from "react";

import { API_BASE_URL } from "@/lib/api-base";
import {
  HERO_QUOTE_REFRESH_INTERVAL_MS,
  HERO_QUOTE_WS_SUBSCRIBED_INTERVAL_MS,
  ORDERBOOK_REFRESH_INTERVAL_MS,
  ORDERBOOK_WS_SUBSCRIBED_INTERVAL_MS,
  STOCK_INVESTOR_TREND_DAYS,
  STOCK_NEWS_PAGE_SIZE,
} from "@/lib/stock/stockDetailConstants";
import { mergePriceResponse } from "@/lib/stock/stockDetailPrice";
import { normalizeOrderbookResponse } from "@/lib/stock/stockDetailOrderbook";
import type {
  InvestorTrendResponse,
  NewsLoadPhase,
  NewsResponse,
  OrderbookResponse,
  PriceResponse,
  StaticProfileResponse,
  StockDetailResponse,
  TabKey,
} from "@/lib/stock/stockDetailTypes";

export function useStockDetailData(stockCode: string, activeTab: TabKey) {
  const [price, setPrice] = useState<PriceResponse | null>(null);
  const [orderbook, setOrderbook] = useState<OrderbookResponse | null>(null);
  const [profile, setProfile] = useState<StaticProfileResponse | null>(null);
  const [news, setNews] = useState<NewsResponse[]>([]);
  const [investorTrend, setInvestorTrend] = useState<InvestorTrendResponse | null>(
    null
  );
  const [detailLoading, setDetailLoading] = useState(true);
  const [orderbookLoading, setOrderbookLoading] = useState(true);
  const [newsPhase, setNewsPhase] = useState<NewsLoadPhase>("idle");
  const [investorLoading, setInvestorLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const snapshotSessionRef = useRef(0);
  const newsFetchInFlightRef = useRef(false);
  const newsDoneSessionRef = useRef<number | null>(null);
  const investorFetchInFlightRef = useRef(false);
  const investorDoneSessionRef = useRef<number | null>(null);

  const fetchJson = useCallback(async <T,>(path: string): Promise<T> => {
    const response = await fetch(`${API_BASE_URL}${path}`, {
      credentials: "include",
      cache: "no-store",
    });

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }

    return response.json() as Promise<T>;
  }, []);

  const runNewsFetch = useCallback(
    async (session: number) => {
      if (session !== snapshotSessionRef.current) {
        return;
      }

      if (newsFetchInFlightRef.current) {
        return;
      }

      if (newsDoneSessionRef.current === session) {
        return;
      }

      newsFetchInFlightRef.current = true;
      setNewsPhase("loading");

      try {
        const newsData = await fetchJson<NewsResponse[]>(
          `/api/public/news/stock/${stockCode}?size=${STOCK_NEWS_PAGE_SIZE}`
        );

        if (session !== snapshotSessionRef.current) {
          return;
        }

        setNews(Array.isArray(newsData) ? newsData : []);
        newsDoneSessionRef.current = session;
      } catch {
        if (session !== snapshotSessionRef.current) {
          return;
        }

        setNews([]);
        newsDoneSessionRef.current = session;
      } finally {
        newsFetchInFlightRef.current = false;

        if (session === snapshotSessionRef.current) {
          setNewsPhase("done");
        }
      }
    },
    [fetchJson, stockCode]
  );

  const runInvestorFetch = useCallback(
    async (session: number) => {
      if (session !== snapshotSessionRef.current) {
        return;
      }

      if (investorFetchInFlightRef.current) {
        return;
      }

      if (investorDoneSessionRef.current === session) {
        return;
      }

      investorFetchInFlightRef.current = true;
      setInvestorLoading(true);

      try {
        const response = await fetch(
          `${API_BASE_URL}/api/stocks/${stockCode}/investor-trend?days=${STOCK_INVESTOR_TREND_DAYS}`,
          {
            credentials: "include",
            cache: "no-store",
          }
        );

        const payload = (await response.json()) as InvestorTrendResponse & {
          success?: boolean;
          message?: string;
        };

        if (session !== snapshotSessionRef.current) {
          return;
        }

        if (!response.ok) {
          setInvestorTrend({
            stockCode,
            summary: null,
            rows: [],
            notice: payload.message ?? `매매동향 조회 실패 (HTTP ${response.status})`,
          });
          return;
        }

        setInvestorTrend(payload);

        if (payload.rows?.length) {
          investorDoneSessionRef.current = session;
        }
      } catch {
        if (session !== snapshotSessionRef.current) {
          return;
        }

        setInvestorTrend({
          stockCode,
          summary: null,
          rows: [],
          notice: "매매동향을 불러오지 못했습니다. 백엔드 서버 연결을 확인해 주세요.",
        });
      } finally {
        investorFetchInFlightRef.current = false;

        if (session === snapshotSessionRef.current) {
          setInvestorLoading(false);
        }
      }
    },
    [stockCode]
  );

  const loadSnapshot = useCallback(async () => {
    setError(null);

    snapshotSessionRef.current += 1;
    const session = snapshotSessionRef.current;

    newsDoneSessionRef.current = null;
    setNewsPhase("idle");
    setNews([]);
    investorDoneSessionRef.current = null;
    setInvestorTrend(null);
    setInvestorLoading(false);
    setPrice(null);
    setProfile(null);
    setOrderbook(null);

    setDetailLoading(true);
    setOrderbookLoading(true);

    let detailOk = false;
    let orderbookOk = false;

    await Promise.all([
      (async () => {
        try {
          const detail = await fetchJson<StockDetailResponse>(
            `/api/stocks/${stockCode}/detail`
          );

          if (session !== snapshotSessionRef.current) {
            return;
          }

          setPrice(detail.price);
          setProfile(detail.profile);
          detailOk = true;
        } catch {
          if (session !== snapshotSessionRef.current) {
            return;
          }

          setPrice(null);
          setProfile(null);
        } finally {
          if (session === snapshotSessionRef.current) {
            setDetailLoading(false);
          }
        }
      })(),
      (async () => {
        try {
          const orderbookData = await fetchJson<OrderbookResponse>(
            `/api/stocks/${stockCode}/orderbook`
          );

          if (session !== snapshotSessionRef.current) {
            return;
          }

          setOrderbook(normalizeOrderbookResponse(orderbookData));
          orderbookOk = true;
        } catch {
          if (session !== snapshotSessionRef.current) {
            return;
          }

          setOrderbook(null);
        } finally {
          if (session === snapshotSessionRef.current) {
            setOrderbookLoading(false);
          }
        }
      })(),
    ]);

    if (session !== snapshotSessionRef.current) {
      return;
    }

    if (!detailOk && !orderbookOk) {
      setError("종목 정보를 불러오지 못했습니다.");
    }
  }, [fetchJson, stockCode]);

  const refreshPrice = useCallback(async () => {
    try {
      const priceData = await fetchJson<PriceResponse>(
        `/api/stocks/${stockCode}/price`
      );
      setPrice((prev) => mergePriceResponse(priceData, prev));
    } catch {
      return;
    }
  }, [fetchJson, stockCode]);

  const heroQuoteIntervalMs =
    price?.wsSubscribed === true
      ? HERO_QUOTE_WS_SUBSCRIBED_INTERVAL_MS
      : HERO_QUOTE_REFRESH_INTERVAL_MS;

  const refreshOrderbook = useCallback(async () => {
    try {
      const orderbookData = await fetchJson<OrderbookResponse>(
        `/api/stocks/${stockCode}/orderbook`
      );
      setOrderbook(normalizeOrderbookResponse(orderbookData));
    } catch {
      return;
    }
  }, [fetchJson, stockCode]);

  const orderbookPollIntervalMs =
    orderbook?.wsSubscribed === true
      ? ORDERBOOK_WS_SUBSCRIBED_INTERVAL_MS
      : ORDERBOOK_REFRESH_INTERVAL_MS;

  const unsubscribeOrderbookWs = useCallback(async () => {
    try {
      await fetch(
        `${API_BASE_URL}/api/stocks/${stockCode}/orderbook/subscribe`,
        {
          method: "DELETE",
          credentials: "include",
        }
      );
    } catch {
      return;
    }
  }, [stockCode]);

  const subscribeOrderbookWs = useCallback(async () => {
    try {
      await fetch(
        `${API_BASE_URL}/api/stocks/${stockCode}/orderbook/subscribe`,
        {
          method: "POST",
          credentials: "include",
        }
      );
    } catch {
      return;
    }
  }, [stockCode]);

  useEffect(() => {
    void loadSnapshot();
  }, [loadSnapshot]);

  useEffect(() => {
    void refreshPrice();

    const timer = window.setInterval(refreshPrice, heroQuoteIntervalMs);

    return () => window.clearInterval(timer);
  }, [refreshPrice, heroQuoteIntervalMs]);

  useEffect(() => {
    if (activeTab !== "orderbook") {
      return;
    }

    let cancelled = false;

    const run = async () => {
      await subscribeOrderbookWs();
      if (!cancelled) {
        await refreshOrderbook();
      }
    };

    void run();

    const timer = window.setInterval(refreshOrderbook, orderbookPollIntervalMs);

    return () => {
      cancelled = true;
      window.clearInterval(timer);
      void unsubscribeOrderbookWs();
    };
  }, [
    activeTab,
    refreshOrderbook,
    subscribeOrderbookWs,
    unsubscribeOrderbookWs,
    orderbookPollIntervalMs,
  ]);

  useEffect(() => {
    if (activeTab !== "news") {
      return;
    }

    if (detailLoading || orderbookLoading) {
      return;
    }

    void runNewsFetch(snapshotSessionRef.current);
  }, [activeTab, runNewsFetch, detailLoading, orderbookLoading]);

  useEffect(() => {
    if (activeTab !== "investor" && activeTab !== "summary") {
      return;
    }

    void runInvestorFetch(snapshotSessionRef.current);
  }, [activeTab, runInvestorFetch]);

  return {
    price,
    orderbook,
    profile,
    news,
    investorTrend,
    detailLoading,
    orderbookLoading,
    newsPhase,
    investorLoading,
    error,
    fetchJson,
  };
}
