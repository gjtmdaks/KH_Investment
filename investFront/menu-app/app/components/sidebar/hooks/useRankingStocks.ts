"use client";

import {
  useEffect,
  useState,
} from "react";

import { apiClient } from "@/lib/api-client";

export type RankingSortKey =
  | "total"
  | "oneday";

export type RankingItem = {
  userNo: number;
  userName: string;
  evaluationAmount: number;
  profitRate: number;
  profitAmount: number;
};

export default function useRankingStocks(
  sortKey: RankingSortKey
) {

  const [loading, setLoading] = useState(true);
  const [rankings, setRankings] = useState<RankingItem[]>([]);

  useEffect(() => {
    async function fetchRankings() {
      try {
        setLoading(true);

        const response =
          await apiClient.get(
            `/account/ranking`,
            {
              params: {
                type: sortKey,
              },
            }
          );

        setRankings(response.data ?? []);

      } catch (error) {
        console.error(
          "[ranking] load fail",
          error
        );

        setRankings([]);

      } finally {
        setLoading(false);
      }
    }

    fetchRankings();
  }, [sortKey]);

  return {
    loading,
    rankings,
  };
}