"use client";

import { useEffect, useState } from "react";
import { apiClient } from "@/lib/api-client";
import { SidebarStock } from "../types";

const RECENT_STOCK_REFRESH_INTERVAL_MS = 5_000;

export default function useRecentStocks(enabled: boolean) {
  const [loading, setLoading] = useState(enabled);
  const [stocks, setStocks] = useState<SidebarStock[]>([]);

  useEffect(() => {
    if (!enabled) {
      const resetTimer = window.setTimeout(() => {
        setStocks([]);
        setLoading(false);
      }, 0);
      return () => window.clearTimeout(resetTimer);
    }

    async function fetchRecentStocks() {
      setLoading(true);

      try {
        const response = await apiClient.get("/watchlist/recent", {
          skipAuthRedirect: true,
        });

        setStocks(response.data.data || []);
      } catch (e) {
        console.error(e);
        setStocks([]);
      } finally {
        setLoading(false);
      }
    }

    fetchRecentStocks();

    const interval = setInterval(() => {
      fetchRecentStocks();
    }, RECENT_STOCK_REFRESH_INTERVAL_MS);

    return () => {
      clearInterval(interval);
    };
  }, [enabled]);

  return {
    loading,
    stocks,
  };
}
