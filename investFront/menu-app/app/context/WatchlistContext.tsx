"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react";

import { useAuth } from "@/app/context/AuthContext";
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
  const { isAuthenticated, isLoading: authLoading } = useAuth();
  const [watchlist, setWatchlist] =
    useState<string[]>([]);

  const refreshWatchlist = useCallback(async () => {
    if (!isAuthenticated) {
      setWatchlist([]);
      return;
    }

    try {
      const { data } = await apiClient.get("/watchlist", {
        skipAuthRedirect: true,
      });

      setWatchlist(data?.data?.watchlist || []);
    } catch (e) {
      console.error(e);
      setWatchlist([]);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    if (authLoading) {
      return;
    }

    refreshWatchlist();
  }, [authLoading, refreshWatchlist]);

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
