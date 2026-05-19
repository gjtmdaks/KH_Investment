"use client";

import { useState } from "react";
import { apiClient } from "@/lib/api-client";
import { useWatchlist } from "@/app/context/WatchlistContext";

interface Params {
  stockCode: string;
  liked: boolean;
  watchlist: string[];
  setWatchlist: React.Dispatch<
    React.SetStateAction<string[]>
  >;
}

type ApiEnvelope = {
  success?: boolean;
  message?: string;
};

function assertApiSuccess(response: { data?: ApiEnvelope }) {
  if (response.data?.success === false) {
    throw new Error(response.data.message ?? "요청에 실패했습니다.");
  }
}

export default function useToggleWatchlist() {
  const [loading, setLoading] = useState(false);
  const { refreshWatchlist } = useWatchlist();

  async function toggleWatchlist(
    e: React.MouseEvent,
    {
      stockCode,
      liked,
      watchlist,
      setWatchlist,
    }: Params
  ) {
    e.preventDefault();
    e.stopPropagation();

    if (loading) return;

    if (!liked && watchlist.length >= 50) {
      alert("관심종목은 최대 50개까지 가능합니다.");
      return;
    }

    setLoading(true);

    const previousWatchlist = watchlist;

    try {
      if (liked) {
        setWatchlist((prev) =>
          prev.filter((code) => code !== stockCode)
        );
      } else {
        setWatchlist((prev) => [...prev, stockCode]);
      }

      try {
        if (liked) {
          const response = await apiClient.delete(
            `/watchlist/${stockCode}`
          );
          assertApiSuccess(response);
        } else {
          const response = await apiClient.post(
            `/watchlist/${stockCode}`
          );
          assertApiSuccess(response);
        }

        await refreshWatchlist();
      } catch (error) {
        console.error(error);
        setWatchlist(previousWatchlist);

        const message =
          error instanceof Error
            ? error.message
            : "관심종목 저장에 실패했습니다.";
        alert(message);
      }
    } finally {
      setLoading(false);
    }
  }

  return {
    loading,
    toggleWatchlist,
  };
}
