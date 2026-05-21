"use client";

import styles from "../MainSidebar.module.css";

type Props = {
  sortKey: string;
  rank: number;
  userName: string;
  profitRate: number;
  evaluationAmount: number;
  profitAmount: number;
};

function formatMoney(value: number) {
  return new Intl.NumberFormat(
    "ko-KR"
  ).format(value);
}

function formatRate(value: number) {
  return value.toFixed(2);
}

export default function SidebarUserItem({
  sortKey,
  rank,
  userName,
  profitRate,
  evaluationAmount,
  profitAmount,
}: Props) {

  const isPositive = profitRate >= 0;

  return (
    <div className={styles.rankingItem}>
      <div className={styles.rankingLeft}>
        <div
          className={`${styles.rankBadge}
          ${rank === 1
            ? styles.gold
            : rank === 2
            ? styles.silver
            : rank === 3
            ? styles.bronze
            : ""}`}
        >
          {rank}
        </div>

        <div className={styles.userInfo}>
          <div className={styles.userName}>
            {userName}
          </div>

          {sortKey === "total" ? (
          <div className={styles.asset}>
            ₩{formatMoney(evaluationAmount)}
          </div>
          ) : (
          <div
            className={`${styles.asset}
              ${profitAmount > 0
              ? styles.plus
              : profitAmount < 0
              ? styles.minus
              : styles.soso}`}
          >
            {profitAmount > 0 ? "+" : ""}
            {formatMoney(profitAmount)}
          </div>
          )}
        </div>
      </div>

      <div
        className={`${styles.profitRate}
        ${isPositive
          ? styles.plus
          : styles.minus}`}
      >
        {isPositive ? "+" : ""}
        {formatRate(profitRate)}%
      </div>
    </div>
  );
}