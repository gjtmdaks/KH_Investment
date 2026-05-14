"use client";

import { useEffect, useState } from "react";
import { apiClient } from "@/lib/api-client";
import { SidebarStock } from "../types";

export default function useRecentStocks() {
  const [loading, setLoading] = useState(true);
  const [stocks, setStocks] = useState<SidebarStock[]>([]);

  useEffect(() => {
    let interval: NodeJS.Timeout;

    async function fetchRecentStocks() {
      try {
        const response =
          await apiClient.get(
            "/watchlist/recent"
          );

        setStocks(response.data.data || []);
      } catch (e) {
        console.error(e);
      } finally {
        setLoading(false);
      }
    }

    // 최초 실행
    fetchRecentStocks();

    // 3초 polling
    interval = setInterval(() => {
      fetchRecentStocks();
    }, 3000);

    return () => {
      clearInterval(interval);
    };
  }, []);

  return {
    loading,
    stocks,
  };
}