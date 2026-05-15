"use client";

import styles from "../../ScreenerPage.module.css";

export default function FilterPanel({
  market,
  setMarket,
  changeRate,
  setChangeRate,
  volume,
  setVolume,
}: any) {
  return (
    <section className={styles.filterPanel}>
      <h2>직접 찾기</h2>

      <div className={styles.filterGrid}>
        <select
          value={market}
          onChange={(e) => setMarket(e.target.value)}
        >
          <option value="">전체 시장</option>
          <option value="KOSPI">KOSPI</option>
          <option value="KOSDAQ">KOSDAQ</option>
        </select>

        <select
          value={changeRate}
          onChange={(e) => setChangeRate(e.target.value)}
        >
          <option value="">등락률</option>
          <option value="UP5">+5% 이상</option>
          <option value="DOWN5">-5% 이하</option>
        </select>

        <select
          value={volume}
          onChange={(e) => setVolume(e.target.value)}
        >
          <option value="">거래량</option>
          <option value="TOP">상위 거래량</option>
        </select>
      </div>
    </section>
  );
}