"use client";

import { useEffect, useRef, useState } from "react";
import { apiClient } from "@/lib/api-client";
import { SidebarStock } from "../types";

const RECENT_STOCK_REFRESH_INTERVAL_MS = 5_000;
const RECENT_STOCK_REQUEST_TIMEOUT_MS = 5_000;

export default function useRecentStocks(enabled: boolean) {
  const [loading, setLoading] = useState(enabled);
  const [stocks, setStocks] = useState<SidebarStock[]>([]);
  const requestInFlightRef = useRef(false);
  const requestControllerRef = useRef<AbortController | null>(null);
  const unmountedRef = useRef(false);

  useEffect(() => {
    unmountedRef.current = false;

    if (!enabled) {
      requestControllerRef.current?.abort();
      requestInFlightRef.current = false;

      const resetTimer = window.setTimeout(() => {
        setStocks([]);
        setLoading(false);
      }, 0);
      return () => window.clearTimeout(resetTimer);
    }

    async function fetchRecentStocks() {
      if (requestInFlightRef.current) {
        return;
      }

      const controller = new AbortController();
      requestControllerRef.current = controller;
      requestInFlightRef.current = true;

      const timeoutId = window.setTimeout(() => {
        controller.abort();
      }, RECENT_STOCK_REQUEST_TIMEOUT_MS);

      setLoading(true);

      try {
        const response = await apiClient.get("/watchlist/recent", {
          skipAuthRedirect: true,
          signal: controller.signal,
          timeout: RECENT_STOCK_REQUEST_TIMEOUT_MS,
        });

        if (!unmountedRef.current) {
          setStocks(response.data.data || []);
        }
      } catch (e) {
        if (!controller.signal.aborted) {
          console.error(e);

          if (!unmountedRef.current) {
            setStocks([]);
          }
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
    }

    fetchRecentStocks();

    const interval = setInterval(() => {
      fetchRecentStocks();
    }, RECENT_STOCK_REFRESH_INTERVAL_MS);

    return () => {
      unmountedRef.current = true;
      requestControllerRef.current?.abort();
      clearInterval(interval);
    };
  }, [enabled]);

  return {
    loading,
    stocks,
  };
}
