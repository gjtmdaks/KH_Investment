"use client";

import { useEffect, useState } from "react";
import styles from "../myAccount.module.css";
import type { LoginUser } from "@/lib/auth-user";
import { getAccountSummary, type AccountSummary } from "@/lib/account";

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

function formatDate(value: string) {
  if (!value) return "-";

  return new Date(value).toLocaleDateString("ko-KR");
}

function getProfitClass(value: number) {
  if (value > 0) return styles.profitUp;
  if (value < 0) return styles.profitDown;
  return styles.profitFlat;
}

export default function AccountManagePanel({ user }: Props) {
  const [accountSummary, setAccountSummary] = useState<AccountSummary | null>(
    null
  );
  const [loading, setLoading] = useState(true);

  useEffect(() => {
  async function fetchAccountSummary() {
    try {
      const data = await getAccountSummary(user.userNo);
      setAccountSummary(data);
    } catch (error) {
      console.error(error);
      setAccountSummary(null);
    } finally {
      setLoading(false);
    }
  }

  fetchAccountSummary();
}, [user.userNo]);
    

  if (loading) {
    return (
      <section className={styles.card}>
        <div className={styles.cardHeader}>
          <div>
            <h2>계좌관리</h2>
            <p>계좌 정보를 불러오는 중입니다.</p>
          </div>
        </div>
      </section>
    );
  }

  if (!accountSummary) {
    return (
      <section className={styles.card}>
        <div className={styles.cardHeader}>
          <div>
            <h2>계좌관리</h2>
            <p>계좌 정보를 찾을 수 없습니다.</p>
          </div>
        </div>
      </section>
    );
  }

  return (
    <>
      <section className={styles.summaryGrid}>
        <article className={styles.summaryCard}>
          <span>현재 총자산</span>
          <strong>{formatWon(accountSummary.currentTotalAsset)}</strong>
        </article>

        <article className={styles.summaryCard}>
          <span>전일 대비 손익</span>
          <strong className={getProfitClass(accountSummary.dailyProfitAmount)}>
            {formatWon(accountSummary.dailyProfitAmount)}
          </strong>
        </article>

        <article className={styles.summaryCard}>
          <span>전일 대비 수익률</span>
          <strong className={getProfitClass(accountSummary.dailyProfitRate)}>
            {formatRate(accountSummary.dailyProfitRate)}
          </strong>
        </article>

        <article className={styles.summaryCard}>
          <span>주문 가능 금액</span>
          <strong>{formatWon(accountSummary.availableCash)}</strong>
        </article>
      </section>

      <section className={styles.card}>
        <div className={styles.cardHeader}>
          <div>
            <h2>계좌관리</h2>
            <p>계좌 상태와 자산 정보를 확인할 수 있습니다.</p>
          </div>
        </div>

        <div className={styles.infoList}>
          <div className={styles.infoRow}>
            <span>현재 총자산</span>
            <strong>{formatWon(accountSummary.currentTotalAsset)}</strong>
          </div>

          <div className={styles.infoRow}>
            <span>전일 기준 금액</span>
            <strong>{formatWon(accountSummary.previousTotalAsset)}</strong>
          </div>

          <div className={styles.infoRow}>
            <span>전일 대비 손익</span>
            <strong className={getProfitClass(accountSummary.dailyProfitAmount)}>
              {formatWon(accountSummary.dailyProfitAmount)}
            </strong>
          </div>

          <div className={styles.infoRow}>
            <span>전일 대비 수익률</span>
            <strong className={getProfitClass(accountSummary.dailyProfitRate)}>
              {formatRate(accountSummary.dailyProfitRate)}
            </strong>
          </div>

          <div className={styles.infoRow}>
            <span>주문 가능 금액</span>
            <strong>{formatWon(accountSummary.availableCash)}</strong>
          </div>

          <div className={styles.infoRow}>
            <span>주식 평가 금액</span>
            <strong>{formatWon(accountSummary.stockValue)}</strong>
          </div>

          <div className={styles.infoRow}>
            <span>초기 자본</span>
            <strong>{formatWon(accountSummary.baseCapital)}</strong>
          </div>

          <div className={styles.infoRow}>
            <span>초기 자본 대비 손익</span>
            <strong className={getProfitClass(accountSummary.baseProfitAmount)}>
              {formatWon(accountSummary.baseProfitAmount)}
            </strong>
          </div>

          <div className={styles.infoRow}>
            <span>초기 자본 대비 수익률</span>
            <strong className={getProfitClass(accountSummary.baseProfitRate)}>
              {formatRate(accountSummary.baseProfitRate)}
            </strong>
          </div>

          <div className={styles.infoRow}>
            <span>계좌 상태</span>
            <strong>{accountSummary.accountStatus}</strong>
          </div>

          <div className={styles.infoRow}>
            <span>계좌 생성일</span>
            <strong>{formatDate(accountSummary.createdAt)}</strong>
          </div>
        </div>
      </section>
    </>
  );
}