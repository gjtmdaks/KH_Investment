"use client";

import { useCallback, useMemo, useState, useEffect } from "react";
import { StockDetailChartShell } from "@/app/components/stock/detail/StockDetailChartShell";
import { StockDetailEmptyState } from "@/app/components/stock/detail/StockDetailEmptyState";
import { StockDetailHero } from "@/app/components/stock/detail/StockDetailHero";
import { StockDetailNewsPanel } from "@/app/components/stock/detail/StockDetailNewsPanel";
import { StockDetailOrderbookLoginGate } from "@/app/components/stock/detail/StockDetailOrderbookLoginGate";
import { StockDetailOrderbookPanel } from "@/app/components/stock/detail/StockDetailOrderbookPanel";
import { StockDetailOrderCard } from "@/app/components/stock/detail/StockDetailOrderCard";
import { StockDetailInvestorTrendPanel } from "@/app/components/stock/detail/StockDetailInvestorTrendPanel";
import { StockDetailSummaryPanel } from "@/app/components/stock/detail/StockDetailSummaryPanel";
import { StockDetailCommunityPanel } from "@/app/components/stock/detail/StockDetailCommunityPanel";
import { apiClient } from "@/lib/api-client";
import { stockDetailTabs } from "@/lib/stock/stockDetailConstants";
import { parseNumeric } from "@/lib/stock/stockDetailFormat";
import type { TabKey } from "@/lib/stock/stockDetailTypes";
import { useAuth } from "@/app/context/AuthContext";
import { useStockDetailData } from "./useStockDetailData";
import { useStockDetailDocumentTitle } from "./useStockDetailDocumentTitle";
import styles from "@/app/components/stock/detail/stockDetail.module.css";
import { useStockDetailOrderForm } from "./useStockDetailOrderForm";
import { StockAiReport, StockDetailAiPanel } from "@/app/components/stock/detail/StockDetailAiPanel";
import StockDetailRiskAckModal from "@/app/components/stock/detail/StockDetailRiskAckModal";
import {
  hasHighRiskAckInSession,
  isHighRiskStock,
  setHighRiskAckInSession,
} from "@/lib/stock/isHighRiskStock";

export default function StockDetailClient({ stockCode }: { stockCode: string }) {
  const { isAuthenticated, isLoading: authLoading } = useAuth();

  useEffect(() => {
    if (authLoading || !isAuthenticated) {
      return;
    }

    async function saveRecentView() {
      try {
        await apiClient.post(`/recent-view/${stockCode}`, undefined, {
          skipAuthRedirect: true,
        });
      } catch (e) {
        console.error(e);
      }
    }
    saveRecentView();
  }, [stockCode, isAuthenticated, authLoading]);

  const [activeTab, setActiveTab] = useState<TabKey>("chart");
  const {
    price,
    orderbook,
    profile,
    news,
    investorTrend,
    detailLoading,
    orderbookLoading,
    newsPhase,
    investorLoading,
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

  const [aiReport, setAiReport] = useState<StockAiReport | null>(null);
  const [aiLoading, setAiLoading] = useState(true);
  const [riskModalOpen, setRiskModalOpen] = useState(false);

  useEffect(() => {
    const controller = new AbortController();
    let cancelled = false;

    async function fetchAiReport() {

      try {
        setAiLoading(true);
        setAiReport(null);

        const response =
          await apiClient.get(
            `/api/ai/stock-report/${stockCode}`,
            {
              signal: controller.signal,
              timeout: 5_000,
            }
          );

        if (!cancelled) {
          setAiReport(response.data);
        }

      } catch (e) {
        if (!controller.signal.aborted) {
          console.error(e);
        }

      } finally {
        if (!cancelled) {
          setAiLoading(false);
        }
      }
    }

    const startFetch = () => {
      if (!cancelled) {
        void fetchAiReport();
      }
    };

    if (typeof requestIdleCallback !== "undefined") {
      const idleId = requestIdleCallback(startFetch, { timeout: 1500 });

      return () => {
        cancelled = true;
        controller.abort();
        cancelIdleCallback(idleId);
      };
    }

    const timerId = window.setTimeout(startFetch, 0);

    return () => {
      cancelled = true;
      controller.abort();
      window.clearTimeout(timerId);
    };
  }, [stockCode]);

  const handleOrderbookPriceSelect = useCallback(
    (selectedPrice: string) => {
      const numeric = parseNumeric(selectedPrice);

      setOrderType("LIMIT");
      setOrderPrice(numeric !== null ? String(numeric) : selectedPrice);
    },
    [setOrderPrice, setOrderType]
  );

  const isUp = useMemo(() => {
    const rate = Number(price?.changeRate ?? 0);
    const change = Number(price?.changePrice ?? 0);

    return rate >= 0 && change >= 0;
  }, [price]);

  const displayName = price?.stockName || profile?.stockName || stockCode;

  useStockDetailDocumentTitle(price, displayName);

  useEffect(() => {
    if (detailLoading) {
      return;
    }

    const name = price?.stockName || profile?.stockName || displayName;
    const shouldOpen = isHighRiskStock(name) && !hasHighRiskAckInSession();
    const riskTimer = window.setTimeout(() => {
      setRiskModalOpen(shouldOpen);
    }, 0);

    return () => window.clearTimeout(riskTimer);
  }, [detailLoading, displayName, price?.stockName, profile?.stockName]);

  const handleRiskAckConfirm = useCallback(() => {
    setHighRiskAckInSession();
    setRiskModalOpen(false);
  }, []);

  const handleOpenInvestorTab = useCallback(() => {
    setActiveTab("investor");
  }, []);

  const marketCap = useMemo(() => {
    const currentPrice = parseNumeric(price?.currentPrice);
    const outstandingShares = parseNumeric(profile?.outstandingShares ?? null);

    if (currentPrice === null || outstandingShares === null) {
      return null;
    }

    return currentPrice * outstandingShares;
  }, [price?.currentPrice, profile?.outstandingShares]);

  return (
    <>
      <StockDetailRiskAckModal
        open={riskModalOpen}
        stockName={displayName}
        onConfirm={handleRiskAckConfirm}
      />
    <main className={`${styles.page} ${styles.pageViewport}`}>
      <div className={styles.layout}>
        {/* LEFT */}
        <div className={styles.mainColumn}>
          <StockDetailHero
            marketBadge={profile?.marketType || "VTS"}
            displayName={displayName}
            stockCode={stockCode}
            price={price}
            isUp={isUp}
            marketCap={marketCap}
          />

          {error ? <div className={styles.error}>{error}</div> : null}

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
                !authLoading && !isAuthenticated ? (
                  <StockDetailOrderbookLoginGate>
                    <StockDetailOrderbookPanel
                      orderbook={orderbook}
                      price={price}
                    />
                  </StockDetailOrderbookLoginGate>
                ) : (
                  <StockDetailOrderbookPanel
                    orderbook={orderbook}
                    price={price}
                    onSelectPrice={handleOrderbookPriceSelect}
                  />
                )
              ) : null}
              {investorLoading && activeTab === "investor" ? (
                <StockDetailEmptyState title="매매동향을 불러오는 중입니다." />
              ) : null}
              {!investorLoading && activeTab === "investor" ? (
                <StockDetailInvestorTrendPanel data={investorTrend} />
              ) : null}
              {detailLoading && activeTab === "summary" ? (
                <StockDetailEmptyState title="종목 정보를 불러오는 중입니다." />
              ) : null}
              {!detailLoading && activeTab === "summary" ? (
                <StockDetailSummaryPanel
                  profile={profile}
                  price={price}
                  investorTrend={investorTrend}
                  investorLoading={investorLoading}
                  onOpenInvestorTab={handleOpenInvestorTab}
                />
              ) : null}
              {activeTab === "news" && newsPhase !== "done" ? (
                <StockDetailEmptyState title="뉴스를 불러오는 중입니다." />
              ) : null}
              {activeTab === "news" && newsPhase === "done" ? (
                <StockDetailNewsPanel news={news} />
              ) : null}
              {activeTab === "community" ? (
                <StockDetailCommunityPanel stockCode={stockCode} />
              ) : null}
            </div>
          </section>
        </div>

        {/* RIGHT */}
        <aside className={styles.sidePanel}>
          <StockDetailAiPanel
            report={aiReport}
            loading={aiLoading}
          />
          
          <StockDetailOrderCard
            stockCode={stockCode}
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

        </aside>
      </div>
    </main>
    </>
  );
}
