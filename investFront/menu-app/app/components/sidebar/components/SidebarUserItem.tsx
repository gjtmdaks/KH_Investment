"use client";

import styles from "../MainSidebar.module.css";

type Props = {
  rank: number;
  userName: string;
  profitRate: number;
  evaluationAmount: number;
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
  rank,
  userName,
  profitRate,
  evaluationAmount,
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

          <div className={styles.asset}>
            ₩
            {formatMoney(
              evaluationAmount
            )}
          </div>
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