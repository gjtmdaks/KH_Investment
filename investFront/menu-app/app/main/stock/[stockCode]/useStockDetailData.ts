"use client";

import { useCallback, useEffect, useRef, useState } from "react";

import { API_BASE_URL } from "@/lib/api-base";
import {
  ORDERBOOK_REFRESH_INTERVAL_MS,
  QUOTE_REFRESH_INTERVAL_MS,
  STOCK_NEWS_PAGE_SIZE,
} from "@/lib/stock/stockDetailConstants";
import { normalizeOrderbookResponse } from "@/lib/stock/stockDetailOrderbook";
import type {
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
  const [detailLoading, setDetailLoading] = useState(true);
  const [orderbookLoading, setOrderbookLoading] = useState(true);
  const [newsPhase, setNewsPhase] = useState<NewsLoadPhase>("idle");
  const [error, setError] = useState<string | null>(null);

  const snapshotSessionRef = useRef(0);
  const newsFetchInFlightRef = useRef(false);
  const newsDoneSessionRef = useRef<number | null>(null);

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

  const loadSnapshot = useCallback(async () => {
    setError(null);

    snapshotSessionRef.current += 1;
    const session = snapshotSessionRef.current;

    newsDoneSessionRef.current = null;
    setNewsPhase("idle");
    setNews([]);
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

  const refreshQuote = useCallback(async () => {
    const [priceResult, orderbookResult] = await Promise.allSettled([
      fetchJson<PriceResponse>(`/api/stocks/${stockCode}/price`),
      fetchJson<OrderbookResponse>(`/api/stocks/${stockCode}/orderbook`),
    ]);
    if (priceResult.status === "fulfilled") {
      setPrice(priceResult.value);
    }

    if (orderbookResult.status === "fulfilled") {
      setOrderbook(normalizeOrderbookResponse(orderbookResult.value));
    }

    if (
      priceResult.status === "rejected" &&
      orderbookResult.status === "rejected"
    ) {
      setError("시세 갱신에 실패했습니다.");
    }
  }, [fetchJson, stockCode]);

  useEffect(() => {
    void loadSnapshot();
  }, [loadSnapshot]);

  useEffect(() => {
    if (activeTab === "orderbook") {
      void refreshQuote();
    }
  }, [activeTab, refreshQuote]);

  useEffect(() => {
    const intervalMs =
      activeTab === "orderbook"
        ? ORDERBOOK_REFRESH_INTERVAL_MS
        : QUOTE_REFRESH_INTERVAL_MS;

    const timer = window.setInterval(refreshQuote, intervalMs);

    return () => window.clearInterval(timer);
  }, [activeTab, refreshQuote]);

  useEffect(() => {
    if (activeTab !== "news") {
      return;
    }

    if (detailLoading || orderbookLoading) {
      return;
    }

    void runNewsFetch(snapshotSessionRef.current);
  }, [activeTab, runNewsFetch, detailLoading, orderbookLoading]);

  return {
    price,
    orderbook,
    profile,
    news,
    detailLoading,
    orderbookLoading,
    newsPhase,
    error,
    fetchJson,
  };
}
