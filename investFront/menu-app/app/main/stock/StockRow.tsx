"use client";

import Link from "next/link";
import styles from "./stock.module.css";

export default function StockRow({ stock }: any) {
  const isUp = stock.changeRate >= 0;

  return (
    <Link
      href={`/main/stock/${stock.stockCode}`}
      className={styles.row}
    >
      <div className={styles.stockName}>
        {stock.stockName}
      </div>

      <div className={styles.price}>
        {stock.price?.toLocaleString()}원
      </div>

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

      <div>
        {stock.volume?.toLocaleString()}
      </div>

      <div>
        {stock.tradingValue?.toLocaleString()}
      </div>

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