import Link from "next/link";
import { StockItem } from "./types";
import styles from "../ScreenerPage.module.css";

export default function StockCard({
  stock,
}: {
  stock: StockItem;
}) {
  const isUp = stock.changeRate >= 0;

  return (
    <Link
      href={`/main/stock/${stock.stockCode}`}
      className={styles.stockRow}
    >
      <div className={styles.rowLeft}>
        <div className={styles.stockName}>
          {stock.stockName}
        </div>

        <div className={styles.marketType}>
          {stock.marketType}
        </div>
      </div>

      <div className={styles.rowRight}>
        <div className={styles.currentPrice}>
          {stock.currentPrice?.toLocaleString()}원
        </div>

        <div
          className={
            isUp
              ? styles.upRate
              : styles.downRate
          }
        >
          {isUp ? "+" : ""}
          {stock.changeRate}%
        </div>
      </div>
    </Link>
  );
}