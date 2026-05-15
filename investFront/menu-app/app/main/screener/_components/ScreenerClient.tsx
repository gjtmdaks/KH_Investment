"use client";

import { useEffect, useState } from "react";
import FilterPanel from "./ScreenerClient/FilterPanel";
import StockTable from "./ScreenerClient/StockTable";
import { StockItem } from "./types";

export default function ScreenerClient() {
  const [stocks, setStocks] = useState<StockItem[]>([]);
  const [market, setMarket] = useState("");
  const [changeRate, setChangeRate] = useState("");
  const [volume, setVolume] = useState("");

  async function fetchStocks() {
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

    const res = await fetch(
      `${process.env.NEXT_PUBLIC_API_URL}/stock/screener/search?${params.toString()}`
    );

    const data = await res.json();

    setStocks(data);
  }

  useEffect(() => {
    fetchStocks();
  }, [market, changeRate, volume]);

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