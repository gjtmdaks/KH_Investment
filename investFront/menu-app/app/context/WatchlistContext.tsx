"use client";

import {
  createContext,
  useContext,
  useEffect,
  useState,
} from "react";

import { apiClient } from "@/lib/api-client";

interface WatchlistContextValue {
  watchlist: string[];

  setWatchlist: React.Dispatch<
    React.SetStateAction<string[]>
  >;

  refreshWatchlist: () => Promise<void>;
}

const WatchlistContext =
  createContext<WatchlistContextValue | null>(
    null
  );

export function WatchlistProvider({
  children,
}: {
  children: React.ReactNode;
}) {

  const [watchlist, setWatchlist] =
    useState<string[]>([]);

  async function refreshWatchlist() {
    try {

      const { data } =
        await apiClient.get("/watchlist");

      setWatchlist(
        data?.data?.watchlist || []
      );

    } catch (e) {

      console.error(e);

      setWatchlist([]);
    }
  }

  useEffect(() => {
    refreshWatchlist();
  }, []);

  return (
    <WatchlistContext.Provider
      value={{
        watchlist,
        setWatchlist,
        refreshWatchlist,
      }}
    >
      {children}
    </WatchlistContext.Provider>
  );
}

export function useWatchlist() {
  const context = useContext(WatchlistContext);

  if (!context) {
    throw new Error("useWatchlist must be used inside WatchlistProvider");
  }

  return context;
}