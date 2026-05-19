"use client";

import { useEffect, useState } from "react";
import { apiClient } from "@/lib/api-client";
import { useWatchlist } from "@/app/context/WatchlistContext";
import {
  SidebarStock,
  SidebarWatchResponse,
} from "../types";

export default function useSidebarWatchlist() {

  const [loading, setLoading] = useState(true);
  const {watchlist, setWatchlist,} = useWatchlist();
  const [sidebarData, setSidebarData] =
    useState<SidebarWatchResponse>({
      loggedIn: false,
      hasWatchlist: false,
      watchlistCodes: [],
      stockList: [],
    });

  async function fetchSidebarStocks() {
    try {
      const response = await apiClient.get("/watchlist/sidebar/stocks", {
        skipAuthRedirect: true,
      });

      const data = response.data.data;

      setSidebarData(data);

      // 실제 관심종목 코드만 저장
      setWatchlist(
        data.watchlistCodes ?? []
      );
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchSidebarStocks();

    // 3초마다 갱신
    const interval = setInterval(
      fetchSidebarStocks,
      3000
    );

    return () => clearInterval(interval);
  }, []);

  return {
    loading,
    sidebarData,
    watchlist,
    setWatchlist,
    refresh: fetchSidebarStocks,
  };
}