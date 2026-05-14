"use client";

import type { PriceResponse } from "@/lib/stock/stockDetailTypes";
import {
  formatChange,
  formatKoreanLargeWon,
  formatNumber,
  formatPercent,
  formatWon,
} from "@/lib/stock/stockDetailFormat";

import styles from "./stockDetail.module.css";
import { StockDetailStat } from "./StockDetailStat";

export function StockDetailHero({
  marketBadge,
  displayName,
  stockCode,
  price,
  isUp,
  marketCap,
}: {
  marketBadge: string;
  displayName: string;
  stockCode: string;
  price: PriceResponse | null;
  isUp: boolean;
  marketCap: number | null;
}) {
  return (
    <section className={styles.hero}>
      <div className={styles.heroPrimary}>
        <div className={styles.heroNameRow}>
          <span className={styles.marketBadge}>{marketBadge}</span>
          <h1>{displayName}</h1>
          <span className={styles.heroCode}>{stockCode}</span>
        </div>
        <div className={styles.heroPriceRow}>
          <strong>{formatWon(price?.currentPrice)}</strong>
          <span className={styles.priceDot} aria-hidden>
            ·
          </span>
          <span className={styles.yesterdayLabel}>어제보다</span>
          <span className={isUp ? styles.up : styles.down}>
            {formatChange(price?.changePrice)} ({formatPercent(price?.changeRate)})
          </span>
        </div>
      </div>

      <div className={styles.heroStats}>
        <StockDetailStat label="거래량(주)" value={formatNumber(price?.volume)} />
        <StockDetailStat label="거래대금" value={formatKoreanLargeWon(price?.tradingValue)} />
        <StockDetailStat label="시가총액" value={formatKoreanLargeWon(marketCap)} />
        <StockDetailStat label="고가" value={formatWon(price?.highPrice)} />
        <StockDetailStat label="저가" value={formatWon(price?.lowPrice)} />
      </div>
    </section>
  );
}
