"use client";

import { useEffect, useState } from "react";
import StockList from "./StockList";
import { useWatchlist } from "@/app/context/WatchlistContext";
import { getPublicApiBase } from "@/lib/api-base";

const MAIN_STOCK_REFRESH_INTERVAL_MS = 3_000;
const MAIN_STOCK_REQUEST_TIMEOUT_MS = 5_000;

type Stock = {
  stockCode: string;
  stockName: string;
  price: number;
  changeRate: number;
  volume: number;
  tradingValue: number;
  aiSentiment: string;
  aiSummary: string;
  aiScore: number;
};

type StockClientProps = {
  initialData?: {
    main?: {
      stockList?: Stock[];
    };
  };
};

export default function StockClient({
  initialData,
}: StockClientProps) {

  const [stocks, setStocks] = useState<Stock[]>(initialData?.main?.stockList || []);
  const {watchlist, setWatchlist,} = useWatchlist();

  useEffect(() => {
    let cancelled = false;
    let timerId: ReturnType<typeof setTimeout> | null = null;
    let currentController: AbortController | null = null;

    const scheduleNextRefresh = () => {
      if (cancelled) {
        return;
      }

      timerId = setTimeout(refreshStocks, MAIN_STOCK_REFRESH_INTERVAL_MS);
    };

    const refreshStocks = async () => {
      currentController = new AbortController();
      const timeoutId = setTimeout(
        () => currentController?.abort(),
        MAIN_STOCK_REQUEST_TIMEOUT_MS
      );

      try {
        const res = await fetch(`${getPublicApiBase()}/api/main`, {
          cache: "no-store",
          headers: { Accept: "application/json" },
          signal: currentController.signal,
        });

        if (!res.ok) {
          throw new Error(`HTTP ${res.status}`);
        }

        const json = await res.json();
        const newList = json?.main?.stockList || [];

        if (cancelled || newList.length === 0) {
          return;
        }

        setStocks(
          [...newList].sort(
            (a, b) => (b.tradingValue ?? 0) - (a.tradingValue ?? 0)
          )
        );
      } catch {
      } finally {
        clearTimeout(timeoutId);
        currentController = null;
        scheduleNextRefresh();
      }
    };

    scheduleNextRefresh();

    return () => {
      cancelled = true;

      if (timerId !== null) {
        clearTimeout(timerId);
      }

      currentController?.abort();
    };
  }, []);

  return (
    <div>
      {stocks.length === 0 ? (
        <div>데이터 없음</div>
      ) : (
        <StockList
          stocks={stocks}
          watchlist={watchlist}
          setWatchlist={setWatchlist}
        />
      )}
    </div>
  );
}