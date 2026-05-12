"use client";

import styles from "../myAccount.module.css";
import type { LoginUser } from "@/lib/auth-user";

type Props = {
  user: LoginUser;
};

function formatWon(value: number) {
  return `${value.toLocaleString()}원`;
}

function formatRate(value: number) {
  const sign = value > 0 ? "+" : "";
  return `${sign}${value.toFixed(2)}%`;
}

function getProfitClass(value: number) {
  if (value > 0) return styles.profitUp;
  if (value < 0) return styles.profitDown;
  return styles.profitFlat;
}

export default function AccountManagePanel({ user }: Props) {
  // 나중에 백엔드에서 받아올 값
  const previousBaseAmount = 10000000; // ACCOUNTS.INITIAL_BALANCE
  const currentTotalAsset = 10850000;

  const dailyProfit = currentTotalAsset - previousBaseAmount;
  const dailyProfitRate =
    previousBaseAmount > 0 ? (dailyProfit / previousBaseAmount) * 100 : 0;

  return (
    <>
      <section className={styles.summaryGrid}>
        <article className={styles.summaryCard}>
          <span>현재 총자산</span>
          <strong>{formatWon(currentTotalAsset)}</strong>
        </article>

        <article className={styles.summaryCard}>
          <span>전일 기준 금액</span>
          <strong>{formatWon(previousBaseAmount)}</strong>
        </article>

        <article className={styles.summaryCard}>
          <span>전일 대비 손익</span>
          <strong className={getProfitClass(dailyProfit)}>
            {formatWon(dailyProfit)}
          </strong>
        </article>

        <article className={styles.summaryCard}>
          <span>전일 대비 수익률</span>
          <strong className={getProfitClass(dailyProfitRate)}>
            {formatRate(dailyProfitRate)}
          </strong>
        </article>
      </section>

      <section className={styles.card}>
        <div className={styles.cardHeader}>
          <div>
            <h2>계좌관리</h2>
            <p>계좌 상태와 전일 대비 수익률 정보를 확인할 수 있습니다.</p>
          </div>
        </div>

        <div className={styles.infoList}>
          <div className={styles.infoRow}>
            <span>회원</span>
            <strong>{user.userName}</strong>
          </div>

          <div className={styles.infoRow}>
            <span>전일 기준 금액</span>
            <strong>{formatWon(previousBaseAmount)}</strong>
          </div>

          <div className={styles.infoRow}>
            <span>현재 총자산</span>
            <strong>{formatWon(currentTotalAsset)}</strong>
          </div>

          <div className={styles.infoRow}>
            <span>전일 대비 손익</span>
            <strong className={getProfitClass(dailyProfit)}>
              {formatWon(dailyProfit)}
            </strong>
          </div>

          <div className={styles.infoRow}>
            <span>전일 대비 수익률</span>
            <strong className={getProfitClass(dailyProfitRate)}>
              {formatRate(dailyProfitRate)}
            </strong>
          </div>

          <div className={styles.infoRow}>
            <span>계좌 상태</span>
            <strong>정상</strong>
          </div>

          <div className={styles.infoRow}>
            <span>계좌 생성일</span>
            <strong>준비 중</strong>
          </div>
        </div>
      </section>
    </>
  );
}