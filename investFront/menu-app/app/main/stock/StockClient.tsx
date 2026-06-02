"use client";

import { useEffect, useState } from "react";
import StockList from "./StockList";
import { useWatchlist } from "@/app/context/WatchlistContext";
import { getPublicApiBase } from "@/lib/api-base";

const MAIN_STOCK_REFRESH_INTERVAL_MS = 3_000;

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
    // 3초마다 실시간 값 갱신
    const realtimeInterval = setInterval(() => {
      fetch(`${getPublicApiBase()}/api/main`)
      .then((res) => {
        if (!res.ok) {
          throw new Error(`HTTP ${res.status}`);
        }
        return res.json();
      })
      .then((json) => {
        const newList = json?.main?.stockList || [];

        if (newList.length === 0) {
          return;
        }

        setStocks(
          [...newList].sort(
            (a, b) => (b.tradingValue ?? 0) - (a.tradingValue ?? 0)
          )
        );
      })
      .catch(() => { });
    }, MAIN_STOCK_REFRESH_INTERVAL_MS);

    return () => {
      clearInterval(realtimeInterval);
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