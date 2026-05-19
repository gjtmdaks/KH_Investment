"use client";

import Link from "next/link";
import styles from "../MainSidebar.module.css";
import { SidebarStock } from "../types";
import useToggleWatchlist from "@/app/hooks/useToggleWatchlist";

interface Props {
  stock: SidebarStock;
  watchlist: string[];
  setWatchlist: React.Dispatch<
    React.SetStateAction<string[]>
  >;
}

export default function SidebarStockItem({
  stock,
  watchlist,
  setWatchlist,
}: Props) {
  const liked = watchlist.includes(stock.stockCode);
  const isUp = stock.changeRate >= 0;
  const { toggleWatchlist } = useToggleWatchlist();

  return (
    <div className={styles.stockItem}>
      <Link
        href={`/main/stock/${stock.stockCode}`}
        className={styles.stockItemLink}
      >
        <div className={styles.stockLogo}>
          {stock.stockName.slice(0, 1)}
        </div>

        <div className={styles.stockName}>
          {stock.stockName}
        </div>

        <div className={styles.stockPrice}>
          <strong>
            {Number(stock.currentPrice).toLocaleString()}원
          </strong>

          <span className={isUp ? styles.up : styles.down}>
            {isUp ? "+" : ""}
            {stock.changeRate}%
          </span>
        </div>
      </Link>

      <button
        type="button"
        className={styles.heartButton}
        aria-label={liked ? "관심 종목 해제" : "관심 종목 추가"}
        onClick={(e) =>
          toggleWatchlist(e, {
            stockCode: stock.stockCode,
            liked,
            watchlist,
            setWatchlist,
          })
        }
      >
        {liked ? "❤️" : "♡"}
      </button>
    </div>
  );
}
