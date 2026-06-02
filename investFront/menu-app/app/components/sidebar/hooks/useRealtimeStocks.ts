"use client";

import { useEffect, useRef, useState } from "react";
import { apiClient } from "@/lib/api-client";
import { SidebarStock } from "../types";

const REALTIME_STOCK_REFRESH_INTERVAL_MS = 5_000;
const REALTIME_STOCK_REQUEST_TIMEOUT_MS = 5_000;

export default function useRealtimeStocks() {
  const [loading, setLoading] = useState(true);
  const [stocks, setStocks] = useState<SidebarStock[]>([]);
  const requestInFlightRef = useRef(false);
  const requestControllerRef = useRef<AbortController | null>(null);
  const unmountedRef = useRef(false);

  useEffect(() => {
    unmountedRef.current = false;

    async function fetchRealtimeStocks() {
      if (requestInFlightRef.current) {
        return;
      }

      const controller = new AbortController();
      requestControllerRef.current = controller;
      requestInFlightRef.current = true;

      const timeoutId = window.setTimeout(() => {
        controller.abort();
      }, REALTIME_STOCK_REQUEST_TIMEOUT_MS);

      try {
        const response = await apiClient.get(
          "/watchlist/realtime",
          {
            signal: controller.signal,
            timeout: REALTIME_STOCK_REQUEST_TIMEOUT_MS,
          }
        );

        if (!unmountedRef.current) {
          setStocks(
            response.data.data || []
          );
        }
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
    }

    fetchRealtimeStocks();

    const interval = setInterval(() => {
      fetchRealtimeStocks();
    }, REALTIME_STOCK_REFRESH_INTERVAL_MS);

    return () => {
      unmountedRef.current = true;
      requestControllerRef.current?.abort();
      clearInterval(interval);
    };
  }, []);

  return {
    loading,
    stocks,
  };
}