"use client";

import { useEffect, useState } from "react";
import { apiClient } from "@/lib/api-client";
import { SidebarStock } from "../types";

const REALTIME_STOCK_REFRESH_INTERVAL_MS = 5_000;

export default function useRealtimeStocks() {
  const [loading, setLoading] = useState(true);
  const [stocks, setStocks] = useState<SidebarStock[]>([]);

  useEffect(() => {
    async function fetchRealtimeStocks() {
      try {
        const response = await apiClient.get(
          "/watchlist/realtime"
        );

        setStocks(
          response.data.data || []
        );
      } catch (e) {
        console.error(e);
      } finally {
        setLoading(false);
      }
    }

    fetchRealtimeStocks();

    const interval = setInterval(() => {
      fetchRealtimeStocks();
    }, REALTIME_STOCK_REFRESH_INTERVAL_MS);

    return () => {
      clearInterval(interval);
    };
  }, []);

  return {
    loading,
    stocks,
  };
}