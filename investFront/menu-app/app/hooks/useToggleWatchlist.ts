"use client";

import { useState } from "react";
import { apiClient } from "@/lib/api-client";

interface Params {
  stockCode: string;

  liked: boolean;

  watchlist: string[];

  setWatchlist: React.Dispatch<
    React.SetStateAction<string[]>
  >;
}

export default function useToggleWatchlist() {

  const [loading, setLoading] =
    useState(false);

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

    if (
      !liked &&
      watchlist.length >= 50
    ) {
      alert(
        "관심종목은 최대 50개까지 가능합니다."
      );
      return;
    }

    setLoading(true);

    try {

      // optimistic update
      if (liked) {

        setWatchlist(prev =>
          prev.filter(
            code => code !== stockCode
          )
        );

      } else {

        setWatchlist(prev => [
          ...prev,
          stockCode,
        ]);
      }

      try {

        if (liked) {

          await apiClient.delete(
            `/watchlist/${stockCode}`
          );

        } else {

          await apiClient.post(
            `/watchlist/${stockCode}`
          );
        }

      } catch (e) {

        console.error(e);

        // rollback
        if (liked) {

          setWatchlist(prev => [
            ...prev,
            stockCode,
          ]);

        } else {

          setWatchlist(prev =>
            prev.filter(
              code => code !== stockCode
            )
          );
        }
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