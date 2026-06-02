"use client";

import { useCallback, useEffect, useState } from "react";
import FilterPanel from "./ScreenerClient/FilterPanel";
import StockTable from "./ScreenerClient/StockTable";
import { StockItem } from "./types";
import { getPublicApiBase } from "@/lib/api-base";

export default function ScreenerClient() {
  const [stocks, setStocks] = useState<StockItem[]>([]);
  const [market, setMarket] = useState("");
  const [changeRate, setChangeRate] = useState("");
  const [volume, setVolume] = useState("");

  const fetchStocks = useCallback(async () => {
    const params = new URLSearchParams();

    if (market) {
      params.append("market", market);
    }

    if (changeRate) {
      params.append("changeRate", changeRate);
    }

    if (volume) {
      params.append("volume", volume);
    }

    try {
      const res = await fetch(
        `${getPublicApiBase()}/stock/screener/search?${params.toString()}`
      );

      const contentType = res.headers.get("content-type") ?? "";

      if (!res.ok || !contentType.includes("application/json")) {
        setStocks([]);
        return;
      }

      const data = await res.json();

      setStocks(Array.isArray(data) ? data : []);
    } catch {
      setStocks([]);
    }
  }, [market, changeRate, volume]);

  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      fetchStocks();
    }, 0);

    return () => {
      window.clearTimeout(timeoutId);
    };
  }, [fetchStocks]);

  return (
    <>
      <FilterPanel
        market={market}
        setMarket={setMarket}
        changeRate={changeRate}
        setChangeRate={setChangeRate}
        volume={volume}
        setVolume={setVolume}
      />

      <StockTable stocks={stocks} />
    </>
  );
}