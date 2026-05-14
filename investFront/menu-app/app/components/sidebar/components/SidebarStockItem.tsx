"use client";

import Link from "next/link";
import styles from "../MainSidebar.module.css";

import { SidebarStock }
from "../types";

import useToggleWatchlist
from "@/app/hooks/useToggleWatchlist";

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

  const liked =
    watchlist.includes(
      stock.stockCode
    );

  const isUp =
    stock.changeRate >= 0;

  const {
    toggleWatchlist,
  } = useToggleWatchlist();

  return (
    <Link
      href={`/main/stock/${stock.stockCode}`}
      className={styles.stockItem}
    >

      <div className={styles.stockLogo}>
        {stock.stockName.slice(0, 1)}
      </div>

      <div className={styles.stockName}>
        {stock.stockName}
      </div>

      <div className={styles.stockPrice}>

        <strong>
          {Number(
            stock.currentPrice
          ).toLocaleString()}원
        </strong>

        <span
          className={
            isUp
              ? styles.up
              : styles.down
          }
        >
          {isUp ? "+" : ""}
          {stock.changeRate}%
        </span>

      </div>

      <button
        type="button"
        className={styles.heartButton}
        onClick={(e) =>
          toggleWatchlist(e, {
            stockCode:
              stock.stockCode,

            liked,

            watchlist,

            setWatchlist,
          })
        }
      >
        {liked ? "❤️" : "♡"}
      </button>

    </Link>
  );
}