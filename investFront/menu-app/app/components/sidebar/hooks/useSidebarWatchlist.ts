"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { useWatchlist } from "@/app/context/WatchlistContext";
import { useAuth } from "@/app/context/AuthContext";
import {
  SidebarWatchResponse,
} from "../types";
import {
  fetchSidebarWatchlist,
  getCachedSidebarWatchlist,
} from "./sidebarPrefetchCache";

const SIDEBAR_WATCHLIST_REFRESH_INTERVAL_MS = 5_000;

export default function useSidebarWatchlist() {

  const { user } = useAuth();
  const userNo = user?.userNo ?? null;
  const cachedSidebarData = getCachedSidebarWatchlist(userNo);
  const [loading, setLoading] = useState(!cachedSidebarData);
  const {watchlist, setWatchlist,} = useWatchlist();
  const requestInFlightRef = useRef(false);
  const unmountedRef = useRef(false);
  const [sidebarData, setSidebarData] =
    useState<SidebarWatchResponse>(cachedSidebarData ?? {
      loggedIn: false,
      hasWatchlist: false,
      watchlistCodes: [],
      stockList: [],
    });

  const fetchSidebarStocks = useCallback(async () => {
    if (requestInFlightRef.current) {
      return;
    }

    requestInFlightRef.current = true;

    try {
      const data = await fetchSidebarWatchlist(userNo);

      if (unmountedRef.current) {
        return;
      }

      setSidebarData(data);

      // 실제 관심종목 코드만 저장
      setWatchlist(
        data.watchlistCodes ?? []
      );
    } catch (e) {
      console.error(e);
    } finally {
      requestInFlightRef.current = false;

      if (!unmountedRef.current) {
        setLoading(false);
      }
    }
  }, [setWatchlist, userNo]);

  useEffect(() => {
    unmountedRef.current = false;

    const initialTimer = window.setTimeout(fetchSidebarStocks, 0);

    // 5초마다 갱신
    const interval = setInterval(
      fetchSidebarStocks,
      SIDEBAR_WATCHLIST_REFRESH_INTERVAL_MS
    );

    return () => {
      unmountedRef.current = true;
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