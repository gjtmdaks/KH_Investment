"use client";

import { apiClient } from "@/lib/api-client";
import type {
  SidebarStock,
  SidebarWatchResponse,
} from "../types";

const SIDEBAR_PREFETCH_TIMEOUT_MS = 5_000;

type CacheKey = number | null;

type CacheEntry<T> = {
  key: CacheKey;
  data: T;
};

let watchlistCache: CacheEntry<SidebarWatchResponse> | null = null;
let recentStocksCache: CacheEntry<SidebarStock[]> | null = null;
let watchlistRequest: Promise<SidebarWatchResponse> | null = null;
let watchlistRequestKey: CacheKey = null;
let recentStocksRequest: Promise<SidebarStock[]> | null = null;
let recentStocksRequestKey: CacheKey = null;

export function getCachedSidebarWatchlist(
  userNo: CacheKey
) {
  return watchlistCache?.key === userNo
    ? watchlistCache.data
    : null;
}

export function getCachedRecentStocks(
  userNo: CacheKey
) {
  return recentStocksCache?.key === userNo
    ? recentStocksCache.data
    : null;
}

export function clearCachedSidebarWatchlist() {
  watchlistCache = null;
}

export function clearCachedRecentStocks() {
  recentStocksCache = null;
}

export async function fetchSidebarWatchlist(
  userNo: CacheKey
) {
  if (watchlistRequest && watchlistRequestKey === userNo) {
    return watchlistRequest;
  }

  watchlistRequestKey = userNo;
  watchlistRequest = apiClient
    .get("/watchlist/sidebar/stocks", {
      skipAuthRedirect: true,
      timeout: SIDEBAR_PREFETCH_TIMEOUT_MS,
    })
    .then((response) => {
      const data = response.data.data as SidebarWatchResponse;
      watchlistCache = {
        key: userNo,
        data,
      };
      return data;
    })
    .finally(() => {
      watchlistRequest = null;
    });

  return watchlistRequest;
}

export async function fetchRecentStocks(
  userNo: CacheKey
) {
  if (userNo == null) {
    recentStocksCache = null;
    return [];
  }

  if (recentStocksRequest && recentStocksRequestKey === userNo) {
    return recentStocksRequest;
  }

  recentStocksRequestKey = userNo;
  recentStocksRequest = apiClient
    .get("/watchlist/recent", {
      skipAuthRedirect: true,
      timeout: SIDEBAR_PREFETCH_TIMEOUT_MS,
    })
    .then((response) => {
      const data = (response.data.data || []) as SidebarStock[];
      recentStocksCache = {
        key: userNo,
        data,
      };
      return data;
    })
    .finally(() => {
      recentStocksRequest = null;
    });

  return recentStocksRequest;
}
