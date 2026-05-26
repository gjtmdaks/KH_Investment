"use client";

import { useId, useState } from "react";

import { INVESTOR_TREND_DATA_GUIDE } from "@/lib/stock/stockDetailInvestorTrend";

import styles from "./css/stockDetailInvestorTrendGuide.module.css";

export function StockDetailInvestorTrendGuide() {
  return (
    <aside className={styles.guide} aria-label="매매동향 데이터 제공 안내">
      <p className={styles.guideTitle}>{INVESTOR_TREND_DATA_GUIDE.title}</p>
      <p className={styles.guideSummary}>{INVESTOR_TREND_DATA_GUIDE.summary}</p>

      <div className={styles.scheduleGrid}>
        <div className={styles.scheduleBlock}>
          <span className={styles.scheduleLabel}>
            {INVESTOR_TREND_DATA_GUIDE.intradayLabel}
          </span>
          <span className={styles.scheduleTimes}>
            {INVESTOR_TREND_DATA_GUIDE.intradayTimes}
          </span>
        </div>
        <div className={styles.scheduleBlock}>
          <span className={styles.scheduleLabel}>
            {INVESTOR_TREND_DATA_GUIDE.afterCloseLabel}
          </span>
          <span className={styles.scheduleTimes}>
            {INVESTOR_TREND_DATA_GUIDE.afterCloseTimes}
          </span>
        </div>
      </div>
    </aside>
  );
}

export function InvestorTrendTodayHelpButton() {
  const [open, setOpen] = useState(false);
  const popoverId = useId();

  return (
    <span className={styles.todayHelpWrap}>
      <button
        type="button"
        className={styles.helpButton}
        aria-label="오늘 매매동향 데이터 안내"
        aria-expanded={open}
        aria-controls={popoverId}
        onClick={() => setOpen((prev) => !prev)}
      >
        ?
      </button>
      {open ? (
        <div
          id={popoverId}
          className={styles.popover}
          role="tooltip"
        >
          <p className={styles.popoverText}>{INVESTOR_TREND_DATA_GUIDE.todayHelp}</p>
          <p className={styles.popoverSub}>
            {INVESTOR_TREND_DATA_GUIDE.intradayLabel}:{" "}
            {INVESTOR_TREND_DATA_GUIDE.intradayTimes}
          </p>
          <p className={styles.popoverSub}>
            {INVESTOR_TREND_DATA_GUIDE.afterCloseLabel}:{" "}
            {INVESTOR_TREND_DATA_GUIDE.afterCloseTimes}
          </p>
        </div>
      ) : null}
    </span>
  );
}
