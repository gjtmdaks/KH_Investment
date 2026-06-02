"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { apiClient } from "@/lib/api-client";
import { useWatchlist } from "@/app/context/WatchlistContext";
import {
  SidebarWatchResponse,
} from "../types";

const SIDEBAR_WATCHLIST_REFRESH_INTERVAL_MS = 5_000;
const SIDEBAR_WATCHLIST_REQUEST_TIMEOUT_MS = 5_000;

export default function useSidebarWatchlist() {

  const [loading, setLoading] = useState(true);
  const {watchlist, setWatchlist,} = useWatchlist();
  const requestInFlightRef = useRef(false);
  const requestControllerRef = useRef<AbortController | null>(null);
  const unmountedRef = useRef(false);
  const [sidebarData, setSidebarData] =
    useState<SidebarWatchResponse>({
      loggedIn: false,
      hasWatchlist: false,
      watchlistCodes: [],
      stockList: [],
    });

  const fetchSidebarStocks = useCallback(async () => {
    if (requestInFlightRef.current) {
      return;
    }

    const controller = new AbortController();
    requestControllerRef.current = controller;
    requestInFlightRef.current = true;

    const timeoutId = window.setTimeout(() => {
      controller.abort();
    }, SIDEBAR_WATCHLIST_REQUEST_TIMEOUT_MS);

    try {
      const response = await apiClient.get("/watchlist/sidebar/stocks", {
        skipAuthRedirect: true,
        signal: controller.signal,
        timeout: SIDEBAR_WATCHLIST_REQUEST_TIMEOUT_MS,
      });

      const data = response.data.data;

      if (unmountedRef.current) {
        return;
      }

      setSidebarData(data);

      // 실제 관심종목 코드만 저장
      setWatchlist(
        data.watchlistCodes ?? []
      );
    } catch (e) {
      if (!controller.signal.aborted) {
        console.error(e);
      }
    } finally {
      window.clearTimeout(timeoutId);
      requestInFlightRef.current = false;

      if (requestControllerRef.current === controller) {
        requestControllerRef.current = null;
      }

      if (!unmountedRef.current) {
        setLoading(false);
      }
    }
  }, [setWatchlist]);

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
      requestControllerRef.current?.abort();
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