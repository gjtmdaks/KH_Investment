"use client";

import { useEffect, useState } from "react";
import { apiClient } from "@/lib/api-client";
import { SidebarStock } from "../types";

export default function useRealtimeStocks() {
  const [loading, setLoading] = useState(true);
  const [stocks, setStocks] = useState<SidebarStock[]>([]);

  useEffect(() => {
    let interval: NodeJS.Timeout;

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

    interval = setInterval(() => {
      fetchRealtimeStocks();
    }, 2000);

    return () => {
      clearInterval(interval);
    };
  }, []);

  return {
    loading,
    stocks,
  };
}