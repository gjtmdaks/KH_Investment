"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { useRouter } from "next/navigation";

import styles from "../MainSidebar.module.css";
import toolbarStyles from "./myInvestmentPanel.module.css";
import SidebarEmpty from "../components/SidebarEmpty";
import { useAuth } from "@/app/context/AuthContext";
import { getAccountAssets } from "@/lib/account";
import type {
  MyInvestmentHolding,
  MyInvestmentSidebarData,
} from "../types";

type Props = {
  data: {
    sidebar?: Partial<MyInvestmentSidebarData>;
  };
};

type ViewMode = "price" | "valuation";

type HoldingSortKey =
  | "name"
  | "totalReturnAsc"
  | "totalReturnDesc"
  | "dailyReturnAsc"
  | "dailyReturnDesc";

const SORT_OPTIONS: { key: HoldingSortKey; label: string }[] = [
  { key: "name", label: "가나다 순" },
  { key: "totalReturnAsc", label: "총 수익률 낮은 순" },
  { key: "totalReturnDesc", label: "총 수익률 높은 순" },
  { key: "dailyReturnAsc", label: "일간 수익률 낮은 순" },
  { key: "dailyReturnDesc", label: "일간 수익률 높은 순" },
];

const LOGIN_REQUIRED_TEXT = "로그인하면 이용할 수 있어요";

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

function parseDailyChangeRate(value: unknown) {
  if (value == null || value === "") {
    return 0;
  }

  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
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

function sortHoldings(
  holdings: MyInvestmentHolding[],
  sortKey: HoldingSortKey
): MyInvestmentHolding[] {
  const list = [...holdings];

  switch (sortKey) {
    case "name":
      return list.sort((a, b) =>
        a.stockName.localeCompare(b.stockName, "ko")
      );
    case "totalReturnAsc":
      return list.sort((a, b) => a.profitRate - b.profitRate);
    case "totalReturnDesc":
      return list.sort((a, b) => b.profitRate - a.profitRate);
    case "dailyReturnAsc":
      return list.sort((a, b) => a.dailyChangeRate - b.dailyChangeRate);
    case "dailyReturnDesc":
      return list.sort((a, b) => b.dailyChangeRate - a.dailyChangeRate);
    default:
      return list;
  }
}

export default function MyInvestmentPanel({ data }: Props) {
  const router = useRouter();
  const { isAuthenticated, isLoading: authLoading } = useAuth();
  const sortMenuRef = useRef<HTMLDivElement>(null);

  const [investment, setInvestment] = useState<MyInvestmentSidebarData>({
    account: data.sidebar?.account ?? null,
    holdings: data.sidebar?.holdings ?? [],
  });
  const [viewMode, setViewMode] = useState<ViewMode>("valuation");
  const [sortKey, setSortKey] = useState<HoldingSortKey>("name");
  const [sortOpen, setSortOpen] = useState(false);

  useEffect(() => {
    setInvestment({
      account: data.sidebar?.account ?? null,
      holdings: data.sidebar?.holdings ?? [],
    });
  }, [data.sidebar?.account, data.sidebar?.holdings]);

  useEffect(() => {
    if (!sortOpen) return;

    function handleClickOutside(event: MouseEvent) {
      if (
        sortMenuRef.current &&
        !sortMenuRef.current.contains(event.target as Node)
      ) {
        setSortOpen(false);
      }
    }

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [sortOpen]);

  useEffect(() => {
    if (!isAuthenticated) return;

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
                dailyChangeRate: parseDailyChangeRate(holding.dailyChangeRate),
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
  }, [isAuthenticated]);

  const sortedHoldings = useMemo(
    () => sortHoldings(investment.holdings, sortKey),
    [investment.holdings, sortKey]
  );

  const activeSortLabel =
    SORT_OPTIONS.find((option) => option.key === sortKey)?.label ?? "가나다 순";

  if (authLoading) {
    return (
      <div className={styles.panelContent}>
        로딩중...
      </div>
    );
  }

  if (!isAuthenticated) {
    return <SidebarEmpty text={LOGIN_REQUIRED_TEXT} />;
  }

  const account = investment.account;
  const holdings = sortedHoldings;

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
          <>
            <div className={toolbarStyles.toolbar}>
              <div className={toolbarStyles.sortWrap} ref={sortMenuRef}>
                <button
                  type="button"
                  className={toolbarStyles.sortButton}
                  onClick={() => setSortOpen((open) => !open)}
                  aria-expanded={sortOpen}
                  aria-haspopup="listbox"
                >
                  {activeSortLabel}
                  <span className={toolbarStyles.sortChevron} aria-hidden>
                    ▾
                  </span>
                </button>

                {sortOpen && (
                  <div
                    className={toolbarStyles.sortMenu}
                    role="listbox"
                  >
                    {SORT_OPTIONS.map((option) => (
                      <button
                        key={option.key}
                        type="button"
                        role="option"
                        aria-selected={sortKey === option.key}
                        className={`${toolbarStyles.sortOption} ${
                          sortKey === option.key
                            ? toolbarStyles.sortOptionActive
                            : ""
                        }`}
                        onClick={() => {
                          setSortKey(option.key);
                          setSortOpen(false);
                        }}
                      >
                        {option.label}
                        {sortKey === option.key && (
                          <span className={toolbarStyles.sortCheck}>✓</span>
                        )}
                      </button>
                    ))}
                  </div>
                )}
              </div>

              <div
                className={toolbarStyles.viewToggle}
                role="group"
                aria-label="표시 방식"
              >
                <button
                  type="button"
                  className={`${toolbarStyles.viewToggleButton} ${
                    viewMode === "price"
                      ? toolbarStyles.viewToggleButtonActive
                      : ""
                  }`}
                  onClick={() => setViewMode("price")}
                >
                  현재가
                </button>
                <button
                  type="button"
                  className={`${toolbarStyles.viewToggleButton} ${
                    viewMode === "valuation"
                      ? toolbarStyles.viewToggleButtonActive
                      : ""
                  }`}
                  onClick={() => setViewMode("valuation")}
                >
                  평가금
                </button>
              </div>
            </div>

            <div className={styles.investHoldingList}>
              {holdings.map((holding) => (
                <article
                  key={holding.stockCode}
                  className={styles.investHoldingItem}
                  role="button"
                  tabIndex={0}
                  onClick={() =>
                    router.push(`/main/stock/${holding.stockCode}`)
                  }
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

                  {viewMode === "price" ? (
                    <div className={styles.investHoldingValue}>
                      <div className={styles.investHoldingValueLeft}>
                        <span>현재가</span>
                        <span className={styles.investHoldingMeta}>
                          내 평균 {formatWon(holding.avgPrice)}
                        </span>
                      </div>
                      <div className={styles.investHoldingValueRight}>
                        <strong>{formatWon(holding.currentPrice)}</strong>
                        <span
                          className={getProfitClass(holding.dailyChangeRate)}
                        >
                          {formatRate(holding.dailyChangeRate)}
                        </span>
                      </div>
                    </div>
                  ) : (
                    <>
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
                    </>
                  )}
                </article>
              ))}
            </div>
          </>
        )}
      </section>
    </div>
  );
}
