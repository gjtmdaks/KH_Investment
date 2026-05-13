"use client";

import { useEffect, useState } from "react";
import StockList from "./StockList";
import { apiClient } from "@/lib/api-client";

const rawBase = process.env.NEXT_PUBLIC_API_URL?.replace(/\/$/, "") ?? "";
const apiBase = rawBase.trim() || "http://localhost:8081/final";

type Stock = {
  stockCode: string;
  stockName: string;
  price: number;
  changeRate: number;
  volume: number;
  tradingValue: number;
};

export default function StockClient({
  initialData,
}: any) {

  const [stocks, setStocks] = useState<Stock[]>(initialData?.main?.stockList || []);
  const [watchlist, setWatchlist] = useState<string[]>([]);

  useEffect(() => {
    // 1초마다 실시간 값 갱신
    const realtimeInterval = setInterval(() => {
      fetch(`${apiBase}/api/main`)
      .then((res) => {
        if (!res.ok) {
          throw new Error(`HTTP ${res.status}`);
        }
        return res.json();
      })
      .then((json) => {
        const newList = json?.main?.stockList || [];

        const realtimeMap =
          new Map<string, Stock>(
            newList.map((s: Stock) => [
              s.stockCode,
              s,
            ])
          );

        setStocks((prev: Stock[]) =>
          prev.map((oldStock) => {

            const updated = realtimeMap.get(oldStock.stockCode);

            if (!updated) {
              return oldStock;
            }

            return {
              ...oldStock,

              price: updated.price,
              changeRate: updated.changeRate,
              volume: updated.volume,
              tradingValue: updated.tradingValue,
            };
          })
        );
      })
      .catch(() => { });
    }, 1000);

    // 10초마다 실제 순위 재정렬
    const rankingInterval = setInterval(() => {
      setStocks((prev: Stock[]) => {
        return [...prev].sort(
          (a, b) => b.changeRate - a.changeRate
        );
      });
    }, 10000);

    // 최초 1회만 로딩
    async function loadWatchlist() {
      try {
        const { data } = await apiClient.get("/watchlist");

        setWatchlist(data.data.watchlist || []);
      } catch (e) {
        console.error(e);
      }
    }
    loadWatchlist();

    return () => {
      clearInterval(realtimeInterval);
      clearInterval(rankingInterval);
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