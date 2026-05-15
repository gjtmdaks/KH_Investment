"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

import styles from "../MainSidebar.module.css";
import SidebarEmpty from "../components/SidebarEmpty";
import { getAccountAssets } from "@/lib/account";
import type {
  MyInvestmentHolding,
  MyInvestmentSidebarData,
} from "../types";

type Props = {
  data: {
    sidebar?: Partial<MyInvestmentSidebarData>;
  };
  isLogin: boolean;
};

function formatWon(value?: number | null) {
  return `${Math.round(value ?? 0).toLocaleString()}원`;
}

function formatQuantity(value?: number | null) {
  return `${(value ?? 0).toLocaleString()}주`;
}

function formatSignedWon(value?: number | null) {
  const numberValue = Math.round(value ?? 0);
  const sign = numberValue > 0 ? "+" : "";

  return `${sign}${numberValue.toLocaleString()}원`;
}

function formatRate(value?: number | null) {
  const numberValue = value ?? 0;
  const sign = numberValue > 0 ? "+" : "";

  return `${sign}${numberValue.toFixed(2)}%`;
}

function getProfitClass(value?: number | null) {
  const numberValue = value ?? 0;

  if (numberValue > 0) return styles.profitUp;
  if (numberValue < 0) return styles.profitDown;
  return styles.profitFlat;
}

export default function MyInvestmentPanel({ data, isLogin }: Props) {
  const router = useRouter();

  const [investment, setInvestment] = useState<MyInvestmentSidebarData>({
    account: data.sidebar?.account ?? null,
    holdings: data.sidebar?.holdings ?? [],
  });

  useEffect(() => {
    setInvestment({
      account: data.sidebar?.account ?? null,
      holdings: data.sidebar?.holdings ?? [],
    });
  }, [data.sidebar?.account, data.sidebar?.holdings]);

  useEffect(() => {
    if (!isLogin) return;

    let isMounted = true;

    async function fetchInvestment() {
      try {
        const asset = await getAccountAssets();

        if (!isMounted) return;

        const holdings: MyInvestmentHolding[] = Array.isArray(asset.holdings)
          ? asset.holdings.map((holding) => {
              const investedAmount = holding.avgPrice * holding.quantity;
              const profitAmount = holding.stockValue - investedAmount;
              const profitRate =
                investedAmount > 0 ? (profitAmount / investedAmount) * 100 : 0;

              return {
                stockCode: holding.stockCode,
                stockName: holding.stockName,
                quantity: holding.quantity,
                avgPrice: holding.avgPrice,
                currentPrice: holding.currentPrice,
                stockValue: holding.stockValue,
                profitAmount,
                profitRate,
              };
            })
          : [];

        const investedAmount = holdings.reduce(
          (sum, holding) => sum + holding.avgPrice * holding.quantity,
          0
        );

        setInvestment({
          account: {
            availableCash: asset.availableCash,
            investedAmount,
          },
          holdings,
        });
      } catch (error) {
        console.error("내 투자 사이드바 조회 실패", error);
      }
    }

    fetchInvestment();

    const intervalId = window.setInterval(() => {
      fetchInvestment();
    }, 2000);

    return () => {
      isMounted = false;
      window.clearInterval(intervalId);
    };
  }, [isLogin]);

  if (!isLogin) {
    return <SidebarEmpty text="로그인이 필요해요" />;
  }

  const account = investment.account;
  const holdings = investment.holdings;

  return (
    <div className={styles.panelContent}>
      <section className={styles.investSummaryBox}>
        <div className={styles.investSummaryRow}>
          <span>가용 가능 금액</span>
          <strong>{formatWon(account?.availableCash)}</strong>
        </div>

        <div className={styles.investSummaryRow}>
          <span>내가 투자한 금액</span>
          <strong>{formatWon(account?.investedAmount)}</strong>
        </div>
      </section>

      <section className={styles.sidebarSection}>
        <div className={styles.sidebarSectionHeader}>
          <h4>보유 주식</h4>
          <span>{holdings.length.toLocaleString()}개</span>
        </div>

        {holdings.length === 0 ? (
          <SidebarEmpty text="보유 주식이 없어요" />
        ) : (
          <div className={styles.investHoldingList}>
            {holdings.map((holding) => (
              <article
                key={holding.stockCode}
                className={styles.investHoldingItem}
                role="button"
                tabIndex={0}
                onClick={() => router.push(`/main/stock/${holding.stockCode}`)}
                onKeyDown={(event) => {
                  if (event.key === "Enter" || event.key === " ") {
                    router.push(`/main/stock/${holding.stockCode}`);
                  }
                }}
              >
                <div className={styles.investHoldingTop}>
                  <div>
                    <strong>{holding.stockName}</strong>
                    <span>{holding.stockCode}</span>
                  </div>

                  <b>{formatQuantity(holding.quantity)}</b>
                </div>

                <div className={styles.investHoldingValue}>
                  <span>평가금액</span>
                  <strong>{formatWon(holding.stockValue)}</strong>
                </div>

                <div className={styles.investHoldingProfit}>
                  <span className={getProfitClass(holding.profitAmount)}>
                    {formatSignedWon(holding.profitAmount)}
                  </span>

                  <span className={getProfitClass(holding.profitRate)}>
                    {formatRate(holding.profitRate)}
                  </span>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}