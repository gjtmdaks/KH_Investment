"use client";

import { useCallback, useEffect, useState } from "react";
import { apiClient } from "@/lib/api-client";
import { useWatchlist } from "@/app/context/WatchlistContext";
import {
  SidebarWatchResponse,
} from "../types";

const SIDEBAR_WATCHLIST_REFRESH_INTERVAL_MS = 5_000;

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

  const fetchSidebarStocks = useCallback(async () => {
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
  }, [setWatchlist]);

  useEffect(() => {
    const initialTimer = window.setTimeout(fetchSidebarStocks, 0);

    // 5초마다 갱신
    const interval = setInterval(
      fetchSidebarStocks,
      SIDEBAR_WATCHLIST_REFRESH_INTERVAL_MS
    );

    return () => {
      window.clearTimeout(initialTimer);
      clearInterval(interval);
    };
  }, [fetchSidebarStocks]);

  return {
    loading,
    sidebarData,
    watchlist,
    setWatchlist,
    refresh: fetchSidebarStocks,
  };
}