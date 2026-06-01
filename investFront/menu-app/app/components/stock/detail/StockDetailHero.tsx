"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";

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

type HeroStatItem = {
  label: string;
  value: string;
};

const HERO_STAT_COLUMN_LABELS = [
  ["거래량(주)", "체결강도"],
  ["거래대금", "고가"],
  ["시가총액", "저가"],
] as const;

function buildHeroStatColumns(
  price: PriceResponse | null,
  marketCap: number | null
): HeroStatItem[][] {
  const values: Record<string, string> = {
    "거래량(주)": formatKoreanLargeShares(price?.volume),
    거래대금: formatKoreanLargeWon(price?.tradingValue),
    시가총액: formatKoreanLargeWon(marketCap),
    체결강도: formatExecutionStrength(price?.executionStrength),
    고가: formatWon(price?.highPrice),
    저가: formatWon(price?.lowPrice),
  };

  return HERO_STAT_COLUMN_LABELS.map((labels) =>
    labels.map((label) => ({
      label,
      value: values[label] ?? "-",
    }))
  );
}

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
  const clipRef = useRef<HTMLDivElement>(null);
  const trackRef = useRef<HTMLDivElement>(null);
  const [canScroll, setCanScroll] = useState(false);
  const [atStart, setAtStart] = useState(true);
  const [atEnd, setAtEnd] = useState(true);

  const statColumns = useMemo(
    () => buildHeroStatColumns(price, marketCap),
    [price, marketCap]
  );

  const syncScrollState = useCallback(() => {
    const clip = clipRef.current;
    const track = trackRef.current;
    if (!clip || !track) {
      return;
    }

    const overflow = track.scrollWidth - clip.clientWidth > 2;
    setCanScroll(overflow);
    setAtStart(clip.scrollLeft <= 2);
    setAtEnd(clip.scrollLeft + clip.clientWidth >= clip.scrollWidth - 2);
  }, []);

  useEffect(() => {
    const clip = clipRef.current;
    const track = trackRef.current;
    if (!clip || !track) {
      return;
    }

    const runSync = () => {
      syncScrollState();
    };

    runSync();
    const rafId = window.requestAnimationFrame(runSync);

    const observer = new ResizeObserver(runSync);
    observer.observe(clip);
    observer.observe(track);

    clip.addEventListener("scroll", runSync, { passive: true });
    window.addEventListener("resize", runSync);

    return () => {
      window.cancelAnimationFrame(rafId);
      observer.disconnect();
      clip.removeEventListener("scroll", runSync);
      window.removeEventListener("resize", runSync);
    };
  }, [syncScrollState, statColumns]);

  useEffect(() => {
    syncScrollState();
  }, [canScroll, syncScrollState]);

  const scrollStats = (direction: -1 | 1) => {
    const el = clipRef.current;
    if (!el) {
      return;
    }

    const column = el.querySelector<HTMLElement>(`.${styles.heroStatsCol}`);
    const gap = 6;
    const step = column ? column.offsetWidth + gap : Math.max(el.clientWidth * 0.9, 120);

    el.scrollBy({ left: direction * step, behavior: "smooth" });
  };

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

      <div
        className={`${styles.heroStatsWrap} ${canScroll ? styles.heroStatsWrapScrollable : ""}`}
      >
        {canScroll ? (
          <button
            type="button"
            className={styles.heroStatsNav}
            aria-label="이전 지표 보기"
            disabled={atStart}
            onClick={() => scrollStats(-1)}
          >
            ‹
          </button>
        ) : null}

        <div
          ref={clipRef}
          className={`${styles.heroStatsClip} ${canScroll ? styles.heroStatsClipScrollable : ""}`}
        >
          <div ref={trackRef} className={styles.heroStats} aria-label="종목 시세 지표">
            {statColumns.map((column, columnIndex) => (
              <div key={columnIndex} className={styles.heroStatsCol}>
                {column.map((item) => (
                  <StockDetailStat
                    key={item.label}
                    className={styles.heroStat}
                    label={item.label}
                    value={item.value}
                  />
                ))}
              </div>
            ))}
          </div>
        </div>

        {canScroll ? (
          <button
            type="button"
            className={styles.heroStatsNav}
            aria-label="다음 지표 보기"
            disabled={atEnd}
            onClick={() => scrollStats(1)}
          >
            ›
          </button>
        ) : null}
      </div>
    </section>
  );
}
