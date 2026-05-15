"use client";

import { useMemo, useState, useEffect } from "react";

import { StockDetailChartShell } from "@/app/components/stock/detail/StockDetailChartShell";
import { StockDetailEmptyState } from "@/app/components/stock/detail/StockDetailEmptyState";
import { StockDetailHero } from "@/app/components/stock/detail/StockDetailHero";
import { StockDetailNewsPanel } from "@/app/components/stock/detail/StockDetailNewsPanel";
import { StockDetailOrderbookPanel } from "@/app/components/stock/detail/StockDetailOrderbookPanel";
import { StockDetailOrderCard } from "@/app/components/stock/detail/StockDetailOrderCard";
import { StockDetailSummaryPanel } from "@/app/components/stock/detail/StockDetailSummaryPanel";
import { apiClient } from "@/lib/api-client";
import { stockDetailTabs } from "@/lib/stock/stockDetailConstants";
import { parseNumeric } from "@/lib/stock/stockDetailFormat";
import type { TabKey } from "@/lib/stock/stockDetailTypes";
import { useStockDetailData } from "./useStockDetailData";
import styles from "@/app/components/stock/detail/stockDetail.module.css";
import { useStockDetailOrderForm } from "./useStockDetailOrderForm";

export default function StockDetailClient({ stockCode }: { stockCode: string }) {
  useEffect(() => {
    const user = localStorage.getItem("user");

    // 비로그인
    if (!user) {
      return;
    }

    async function saveRecentView() {
      try {
        await apiClient.post(
          `/recent-view/${stockCode}`
        );
      } catch (e) {
        console.error(e);
      }
    }
    saveRecentView();
  }, [stockCode]);

  const [activeTab, setActiveTab] = useState<TabKey>("chart");
  const {
    price,
    orderbook,
    profile,
    news,
    detailLoading,
    orderbookLoading,
    newsPhase,
    error,
    fetchJson,
  } = useStockDetailData(stockCode, activeTab);

  const {
    orderKind,
    setOrderKind,
    orderType,
    setOrderType,
    quantity,
    setQuantity,
    orderPrice,
    setOrderPrice,
    orderLoading,
    orderMessage,
    handleCreateOrder,
  } = useStockDetailOrderForm(stockCode, price);

  const isUp = useMemo(() => {
    const rate = Number(price?.changeRate ?? 0);
    const change = Number(price?.changePrice ?? 0);

    return rate >= 0 && change >= 0;
  }, [price]);

  const displayName = price?.stockName || profile?.stockName || stockCode;

  const marketCap = useMemo(() => {
    const currentPrice = parseNumeric(price?.currentPrice);
    const outstandingShares = parseNumeric(profile?.outstandingShares ?? null);

    if (currentPrice === null || outstandingShares === null) {
      return null;
    }

    return currentPrice * outstandingShares;
  }, [price?.currentPrice, profile?.outstandingShares]);

  return (
    <main className={styles.page}>
      <StockDetailHero
        marketBadge={profile?.marketType || "VTS"}
        displayName={displayName}
        stockCode={stockCode}
        price={price}
        isUp={isUp}
        marketCap={marketCap}
      />

      {error ? <div className={styles.error}>{error}</div> : null}

      <div className={styles.layout}>
        <section className={styles.mainPanel}>
          <nav className={styles.tabs}>
            {stockDetailTabs.map((tab) => (
              <button
                key={tab.key}
                type="button"
                className={activeTab === tab.key ? styles.activeTab : ""}
                onClick={() => setActiveTab(tab.key)}
              >
                {tab.label}
              </button>
            ))}
          </nav>

          <div className={styles.panelBody}>
            {activeTab === "chart" ? (
              <StockDetailChartShell stockCode={stockCode} fetchJson={fetchJson} />
            ) : null}
            {orderbookLoading && activeTab === "orderbook" ? (
              <StockDetailEmptyState title="호가 정보를 불러오는 중입니다." />
            ) : null}
            {!orderbookLoading && activeTab === "orderbook" ? (
              <StockDetailOrderbookPanel orderbook={orderbook} />
            ) : null}
            {detailLoading && activeTab === "summary" ? (
              <StockDetailEmptyState title="종목 정보를 불러오는 중입니다." />
            ) : null}
            {!detailLoading && activeTab === "summary" ? (
              <StockDetailSummaryPanel profile={profile} price={price} />
            ) : null}
            {activeTab === "news" && newsPhase !== "done" ? (
              <StockDetailEmptyState title="뉴스를 불러오는 중입니다." />
            ) : null}
            {activeTab === "news" && newsPhase === "done" ? (
              <StockDetailNewsPanel news={news} />
            ) : null}
          </div>
        </section>

        <aside className={styles.sidePanel}>
          <StockDetailOrderCard
            orderKind={orderKind}
            setOrderKind={setOrderKind}
            orderType={orderType}
            setOrderType={setOrderType}
            quantity={quantity}
            setQuantity={setQuantity}
            orderPrice={orderPrice}
            setOrderPrice={setOrderPrice}
            orderLoading={orderLoading}
            orderMessage={orderMessage}
            handleCreateOrder={handleCreateOrder}
            price={price}
          />

          <div className={styles.snapshotCard}>
            <h3>내 주식 요약</h3>
            <p>기능 X.</p>
          </div>
        </aside>
      </div>
    </main>
  );
}
