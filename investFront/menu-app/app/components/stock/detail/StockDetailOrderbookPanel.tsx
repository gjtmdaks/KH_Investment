"use client";

import {
  formatNumber,
  formatWon,
} from "@/lib/stock/stockDetailFormat";
import { normalizeOrderbookLevels } from "@/lib/stock/stockDetailOrderbook";
import type {
  OrderbookLevel,
  OrderbookResponse,
} from "@/lib/stock/stockDetailTypes";

import styles from "./css/stockDetailOrderbookPanel.module.css";
import { StockDetailEmptyState } from "./StockDetailEmptyState";

function OrderbookRow({
  level,
  side,
}: {
  level: OrderbookLevel;
  side: "ask" | "bid";
}) {
  return (
    <div className={`${styles.orderbookRow} ${styles[side]}`}>
      <span>{formatWon(level.price)}</span>
      <span>{formatNumber(level.quantity)}</span>
    </div>
  );
}

export function StockDetailOrderbookPanel({
  orderbook,
}: {
  orderbook: OrderbookResponse | null;
}) {
  if (!orderbook) {
    return <StockDetailEmptyState title="호가 정보를 불러오지 못했습니다." />;
  }

  const asks = normalizeOrderbookLevels(orderbook.asks);
  const bids = normalizeOrderbookLevels(orderbook.bids);

  if (asks.length === 0 && bids.length === 0) {
    return <StockDetailEmptyState title="호가 정보를 불러오지 못했습니다." />;
  }

  return (
    <div className={styles.orderbookGrid}>
      <div className={styles.orderbookSide}>
        <h3>매도 호가</h3>
        {[...asks].reverse().map((level) => (
          <OrderbookRow key={`ask-${level.level}`} level={level} side="ask" />
        ))}
        <div className={styles.totalRow}>
          총 매도잔량 {formatNumber(orderbook.totalAskQuantity)}
        </div>
      </div>

      <div className={styles.orderbookSide}>
        <h3>매수 호가</h3>
        {bids.map((level) => (
          <OrderbookRow key={`bid-${level.level}`} level={level} side="bid" />
        ))}
        <div className={styles.totalRow}>
          총 매수잔량 {formatNumber(orderbook.totalBidQuantity)}
        </div>
      </div>
    </div>
  );
}
