"use client";

import type { PriceResponse } from "@/lib/stock/stockDetailTypes";
import {
  formatChange,
  formatExecutionStrength,
  formatKoreanLargeShares,
  formatKoreanLargeWon,
  formatPercent,
  formatWon,
} from "@/lib/stock/stockDetailFormat";

import styles from "./css/stockDetailHero.module.css";
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

      <div className={styles.heroStatsClip}>
        <div className={styles.heroStats} aria-label="종목 시세 지표">
          <StockDetailStat
            className={styles.heroStat}
            label="거래량(주)"
            value={formatKoreanLargeShares(price?.volume)}
          />
          <StockDetailStat className={styles.heroStat} label="거래대금" value={formatKoreanLargeWon(price?.tradingValue)} />
          <StockDetailStat className={styles.heroStat} label="시가총액" value={formatKoreanLargeWon(marketCap)} />
          <StockDetailStat
            className={styles.heroStat}
            label="체결강도"
            value={formatExecutionStrength(price?.executionStrength)}
          />
          <StockDetailStat className={styles.heroStat} label="고가" value={formatWon(price?.highPrice)} />
          <StockDetailStat className={styles.heroStat} label="저가" value={formatWon(price?.lowPrice)} />
        </div>
      </div>
    </section>
  );
}
