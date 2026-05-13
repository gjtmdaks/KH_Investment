"use client";

import Link from "next/link";
import styles from "./stock.module.css";
import { apiClient } from "@/lib/api-client";
import { useEffect, useState } from "react";

interface Props {
  stock: any;
  rank: number;
  watchlist: string[];
  setWatchlist: any;
}

export default function StockRow({
  stock,
  rank,
  watchlist,
  setWatchlist,
}: Props) {

  const liked = watchlist.includes(stock.stockCode);
  const isUp = stock.changeRate >= 0;
  const [loading, setLoading] = useState(false);

  async function toggleWatchlist(e: React.MouseEvent) {
    e.preventDefault();

    if (loading) return;
    setLoading(true);

    const stockCode = stock.stockCode;
    const currentlyLiked  = watchlist.includes(stock.stockCode);
    
    try {
      // optimistic update
      if (currentlyLiked ) {
        setWatchlist((prev: string[]) =>
          prev.filter(code => code !== stockCode)
        );
      } else {
        setWatchlist((prev: string[]) => [
          ...prev,
          stockCode,
        ]);
      }

      try {
        if (currentlyLiked) {
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
        if (currentlyLiked) {
          setWatchlist((prev: string[]) => [
            ...prev,
            stockCode,
          ]);
        } else {
          setWatchlist((prev: string[]) =>
            prev.filter(code => code !== stockCode)
          );
        }
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <Link
      href={`/main/stock/${stock.stockCode}`}
      className={styles.row}
    >
      {/* 관심종목 */}
      <div
        className={`${styles.favorite} ${
          liked ? styles.favoriteActive : ""
        }`}
        onClick={toggleWatchlist}
      >
        {liked ? "❤️" : "♡"}
      </div>

      {/* 순위 */}
      <div className={styles.rank}>
        {rank}
      </div>

      {/* 종목명 */}
      <div className={styles.stockName}>
        {stock.stockName}
      </div>

      {/* 현재가 */}
      <div className={styles.price}>
        {stock.price?.toLocaleString()}원
      </div>

      {/* 등락률 */}
      <div
        className={
          isUp
            ? styles.up
            : styles.down
        }
      >
        {isUp ? "+" : ""}
        {stock.changeRate}%
      </div>

      {/* 거래량 */}
      <div>
        {stock.volume?.toLocaleString()}
      </div>

      {/* 거래대금 */}
      <div>
        {stock.tradingValue?.toLocaleString()}
      </div>

      {/* 버튼 */}
      <div className={styles.actions}>
        <button
          className={styles.buyBtn}
          onClick={(e) => {
            e.preventDefault();
          }}
        >
          매수
        </button>

        <button
          className={styles.sellBtn}
          onClick={(e) => {
            e.preventDefault();
          }}
        >
          매도
        </button>
      </div>

    </Link>
  );
}