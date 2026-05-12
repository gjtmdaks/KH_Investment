"use client";

import Link from "next/link";
import styles from "./stock.module.css";

interface Props {
  stock: any;
  rank: number;
}

export default function StockRow({
  stock,
  rank,
}: Props) {

  const isUp = stock.changeRate >= 0;

  return (
    <Link
      href={`/main/stock/${stock.stockCode}`}
      className={styles.row}
    >
      {/* 관심종목 */}
      <div
        className={styles.favorite}
        onClick={(e) => {
          e.preventDefault();
          // 관심종목 추가/삭제
        }}
      >
        ♡{/* 클릭 시 ❤️로 변경 */}
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