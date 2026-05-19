"use client";

import Link from "next/link";
import styles from "./stock.module.css";
import useToggleWatchlist from "@/app/hooks/useToggleWatchlist";

interface Props {
  stock: any;
  rank: number;
  watchlist: string[];
  setWatchlist: React.Dispatch<
    React.SetStateAction<string[]>
  >;
}

export default function StockRow({
  stock,
  rank,
  watchlist,
  setWatchlist,
}: Props) {
  const liked = watchlist.includes(stock.stockCode);
  const isUp = stock.changeRate >= 0;
  const { toggleWatchlist, } = useToggleWatchlist();

  return (
    <Link
      href={`/main/stock/${stock.stockCode}`}
      className={styles.row}
    >
      {/* 관심종목 */}
      <div
        className={`${styles.favorite} ${liked
            ? styles.favoriteActive
            : ""
          }`}
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
      </div>

      {/* 순위 */}
      <div className={styles.rank}>
        {rank}
      </div>

      {/* 로고 */}
      <div className={styles.stockLogo}>
        {stock.stockName.slice(0, 1)}
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

      {/* ai분석 */}
      <div className={styles.aiSummary}>
        <span
          className={
            stock.aiSentiment === "POSITIVE"
              ? styles.positive
              : stock.aiSentiment === "NEGATIVE"
                ? styles.negative
                : styles.neutral
          }
        >
          {stock.aiSentiment === "POSITIVE"
            ? "🟢"
            : stock.aiSentiment === "NEGATIVE"
              ? "🔴"
              : "🟡"}
        </span>
        {stock.aiSummary}발표 관망세
      </div>

    </Link>
  );
}