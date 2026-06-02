"use client";

import { useEffect, useRef, useState } from "react";
import { SidebarStock } from "../types";
import {
  fetchRecentStocks,
  getCachedRecentStocks,
} from "./sidebarPrefetchCache";

const RECENT_STOCK_REFRESH_INTERVAL_MS = 5_000;

export default function useRecentStocks(
  enabled: boolean,
  userNo: number | null
) {
  const cachedStocks = getCachedRecentStocks(userNo);
  const [loading, setLoading] = useState(enabled && !cachedStocks);
  const [stocks, setStocks] = useState<SidebarStock[]>(
    cachedStocks ?? []
  );
  const requestInFlightRef = useRef(false);
  const unmountedRef = useRef(false);

  useEffect(() => {
    unmountedRef.current = false;

    if (!enabled) {
      requestInFlightRef.current = false;

      const resetTimer = window.setTimeout(() => {
        setStocks([]);
        setLoading(false);
      }, 0);
      return () => window.clearTimeout(resetTimer);
    }

    async function refreshRecentStocks() {
      if (requestInFlightRef.current) {
        return;
      }

      requestInFlightRef.current = true;

      if (!getCachedRecentStocks(userNo)) {
        setLoading(true);
      }

      try {
        const data = await fetchRecentStocks(userNo);

        if (!unmountedRef.current) {
          setStocks(data);
        }
      } catch (e) {
        console.error(e);

        if (!unmountedRef.current && !getCachedRecentStocks(userNo)) {
          setStocks([]);
        }
      } finally {
        requestInFlightRef.current = false;

        if (!unmountedRef.current) {
          setLoading(false);
        }
      }
    }

    refreshRecentStocks();

    const interval = setInterval(() => {
      refreshRecentStocks();
    }, RECENT_STOCK_REFRESH_INTERVAL_MS);

    return () => {
      unmountedRef.current = true;
      clearInterval(interval);
    };
  }, [enabled, userNo]);

  return {
    loading,
    stocks,
  };
}
