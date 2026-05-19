"use client";

import { useEffect, useState } from "react";
import { apiClient } from "@/lib/api-client";
import { SidebarStock } from "../types";

export default function useRecentStocks(enabled: boolean) {
  const [loading, setLoading] = useState(enabled);
  const [stocks, setStocks] = useState<SidebarStock[]>([]);

  useEffect(() => {
    if (!enabled) {
      setStocks([]);
      setLoading(false);
      return;
    }

    let interval: NodeJS.Timeout;

    async function fetchRecentStocks() {
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

    setLoading(true);
    fetchRecentStocks();

    interval = setInterval(() => {
      fetchRecentStocks();
    }, 3000);

    return () => {
      clearInterval(interval);
    };
  }, [enabled]);

  return {
    loading,
    stocks,
  };
}
