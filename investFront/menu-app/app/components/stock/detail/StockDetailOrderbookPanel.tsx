"use client";

import { useLayoutEffect, useRef } from "react";

import {
  formatNumber,
  formatPercent,
  formatWon,
  parseNumeric,
} from "@/lib/stock/stockDetailFormat";
import { normalizeOrderbookLevels } from "@/lib/stock/stockDetailOrderbook";
import {
  calcDepthPercent,
  calcMaxQuantity,
  calcMidPrice,
  calcPriceChangeRate,
  calcQuantityRatio,
  calcSpread,
  getBestAskBid,
  getLevelPrice,
  getLevelQuantity,
  getReferencePrice,
  isCurrentPriceRow,
} from "@/lib/stock/stockOrderbookMetrics";
import type {
  OrderbookLevel,
  OrderbookResponse,
  PriceResponse,
} from "@/lib/stock/stockDetailTypes";

import styles from "./css/stockDetailOrderbookPanel.module.css";
import { StockDetailEmptyState } from "./StockDetailEmptyState";

type OrderbookSide = "ask" | "bid";

function getPriceToneClass(changeRate: number | null) {
  if (changeRate === null) {
    return styles.priceNeutral;
  }

  if (changeRate > 0) {
    return styles.priceUp;
  }

  if (changeRate < 0) {
    return styles.priceDown;
  }

  return styles.priceNeutral;
}

function OrderbookLadderRow({
  level,
  side,
  maxQuantity,
  referencePrice,
  currentPrice,
  onSelectPrice,
}: {
  level: OrderbookLevel;
  side: OrderbookSide;
  maxQuantity: number;
  referencePrice: number | null;
  currentPrice: number | null;
  onSelectPrice?: (price: string) => void;
}) {
  const levelPrice = getLevelPrice(level);
  const quantity = getLevelQuantity(level);
  const depthPercent = calcDepthPercent(quantity, maxQuantity);
  const changeRate = calcPriceChangeRate(levelPrice, referencePrice);
  const isCurrent = isCurrentPriceRow(levelPrice, currentPrice);
  const isAsk = side === "ask";
  const priceToneClass = getPriceToneClass(changeRate);

  const handleClick = () => {
    if (!onSelectPrice || level.price === null || level.price === undefined) {
      return;
    }

    onSelectPrice(level.price);
  };

  const changeClass =
    changeRate === null
      ? styles.neutralRate
      : changeRate >= 0
        ? styles.upRate
        : styles.downRate;

  return (
    <button
      type="button"
      className={`${styles.ladderRow} ${isCurrent ? styles.currentRow : ""}`}
      onClick={handleClick}
      disabled={!level.price}
    >
      <div className={styles.askCell}>
        {isAsk ? (
          <>
            <span
              className={`${styles.depthBarFill} ${styles.askDepth}`}
              style={{ width: `${depthPercent}%` }}
              aria-hidden
            />
            <span className={styles.quantityText}>{formatNumber(level.quantity)}</span>
          </>
        ) : null}
      </div>

      <div className={styles.priceCell}>
        <span className={`${styles.priceText} ${priceToneClass}`}>
          {formatWon(level.price)}
        </span>
        {changeRate !== null ? (
          <span className={`${styles.rateText} ${changeClass}`}>
            {formatPercent(String(changeRate))}
          </span>
        ) : null}
      </div>

      <div className={styles.bidCell}>
        {!isAsk ? (
          <>
            <span
              className={`${styles.depthBarFill} ${styles.bidDepth}`}
              style={{ width: `${depthPercent}%` }}
              aria-hidden
            />
            <span className={styles.quantityText}>{formatNumber(level.quantity)}</span>
          </>
        ) : null}
      </div>
    </button>
  );
}

function SummaryItem({ label, value }: { label: string; value: string }) {
  return (
    <div className={styles.summaryItem}>
      <span className={styles.summaryLabel}>{label}</span>
      <span className={styles.summaryValue}>{value}</span>
    </div>
  );
}

function scrollChildToVerticalCenter(
  scrollContainer: HTMLElement,
  target: HTMLElement
) {
  const containerRect = scrollContainer.getBoundingClientRect();
  const targetRect = target.getBoundingClientRect();
  const targetOffset =
    targetRect.top - containerRect.top + scrollContainer.scrollTop;
  const nextScrollTop =
    targetOffset - scrollContainer.clientHeight / 2 + target.offsetHeight / 2;

  scrollContainer.scrollTop = Math.max(
    0,
    Math.min(nextScrollTop, scrollContainer.scrollHeight - scrollContainer.clientHeight)
  );
}

export function StockDetailOrderbookPanel({
  orderbook,
  price,
  onSelectPrice,
}: {
  orderbook: OrderbookResponse | null;
  price: PriceResponse | null;
  onSelectPrice?: (price: string) => void;
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
    <StockDetailOrderbookPanelView
      orderbook={orderbook}
      price={price}
      asks={asks}
      bids={bids}
      onSelectPrice={onSelectPrice}
    />
  );
}

function StockDetailOrderbookPanelView({
  orderbook,
  price,
  asks,
  bids,
  onSelectPrice,
}: {
  orderbook: OrderbookResponse;
  price: PriceResponse | null;
  asks: OrderbookLevel[];
  bids: OrderbookLevel[];
  onSelectPrice?: (price: string) => void;
}) {
  const ladderScrollRef = useRef<HTMLDivElement>(null);
  const centerAnchorRef = useRef<HTMLDivElement>(null);

  const asksDesc = [...asks].sort((a, b) => b.level - a.level);
  const bidsAsc = [...bids].sort((a, b) => a.level - b.level);
  const maxQuantity = calcMaxQuantity([...asks, ...bids]);
  const referencePrice = getReferencePrice(price);
  const currentPrice = parseNumeric(price?.currentPrice);
  const { bestAsk, bestBid } = getBestAskBid(asks, bids);
  const spread = calcSpread(bestAsk?.price ?? null, bestBid?.price ?? null);
  const midPrice = calcMidPrice(bestAsk?.price ?? null, bestBid?.price ?? null);
  const totalAsk = parseNumeric(orderbook.totalAskQuantity);
  const totalBid = parseNumeric(orderbook.totalBidQuantity);
  const { askPercent, bidPercent } = calcQuantityRatio(totalAsk, totalBid);

  const currentInAsks = asks.some((level) =>
    isCurrentPriceRow(getLevelPrice(level), currentPrice)
  );
  const currentInBids = bids.some((level) =>
    isCurrentPriceRow(getLevelPrice(level), currentPrice)
  );

  const showCurrentOnlyRow =
    !currentInAsks && !currentInBids && currentPrice !== null;

  useLayoutEffect(() => {
    const scrollEl = ladderScrollRef.current;
    const anchorEl = centerAnchorRef.current;
    if (!scrollEl || !anchorEl) {
      return;
    }

    const centerScroll = () => {
      scrollChildToVerticalCenter(scrollEl, anchorEl);
    };

    centerScroll();
    const rafId = requestAnimationFrame(centerScroll);
    return () => cancelAnimationFrame(rafId);
  }, []);

  return (
    <div className={styles.panel}>
      <div className={styles.summaryBar}>
        <SummaryItem
          label="호가공백"
          value={spread !== null ? `${formatNumber(String(spread))}원` : "-"}
        />
        <SummaryItem
          label="중간호가"
          value={midPrice !== null ? formatWon(String(Math.round(midPrice))) : "-"}
        />
        <SummaryItem
          label="총 매도"
          value={formatNumber(orderbook.totalAskQuantity)}
        />
        <SummaryItem
          label="총 매수"
          value={formatNumber(orderbook.totalBidQuantity)}
        />
        {orderbook.expectedPrice ? (
          <SummaryItem
            label="예상체결"
            value={`${formatWon(orderbook.expectedPrice)} / ${formatNumber(orderbook.expectedQuantity)}`}
          />
        ) : null}
      </div>

      <div className={styles.ladderSection}>
        <div className={styles.ladderHeader}>
          <span>매도잔량</span>
          <span>호가</span>
          <span>매수잔량</span>
        </div>

        <div ref={ladderScrollRef} className={styles.ladderScroll}>
          <div className={styles.ladderBody}>
            {asksDesc.map((level) => (
          <OrderbookLadderRow
            key={`ask-${level.level}`}
            level={level}
            side="ask"
            maxQuantity={maxQuantity}
            referencePrice={referencePrice}
            currentPrice={currentPrice}
            onSelectPrice={onSelectPrice}
          />
        ))}

        {spread !== null ? (
          <div ref={centerAnchorRef} className={styles.spreadRow}>
            <span>호가 공백 {formatNumber(String(spread))}원</span>
          </div>
        ) : showCurrentOnlyRow ? (
          <div
            ref={centerAnchorRef}
            className={`${styles.ladderRow} ${styles.currentRow} ${styles.currentOnlyRow}`}
          >
            <div className={styles.askCell} />
            <div className={styles.priceCell}>
              <span
                className={`${styles.priceText} ${getPriceToneClass(
                  calcPriceChangeRate(currentPrice, referencePrice)
                )}`}
              >
                {formatWon(price?.currentPrice)}
              </span>
              {price?.changeRate ? (
                <span
                  className={`${styles.rateText} ${
                    Number(price.changeRate) >= 0 ? styles.upRate : styles.downRate
                  }`}
                >
                  {formatPercent(price.changeRate)}
                </span>
              ) : null}
            </div>
            <div className={styles.bidCell} />
          </div>
        ) : (
          <div ref={centerAnchorRef} className={styles.spreadAnchor} aria-hidden />
        )}

        {bidsAsc.map((level) => (
          <OrderbookLadderRow
            key={`bid-${level.level}`}
            level={level}
            side="bid"
            maxQuantity={maxQuantity}
            referencePrice={referencePrice}
            currentPrice={currentPrice}
            onSelectPrice={onSelectPrice}
          />
        ))}
          </div>
        </div>
      </div>

      <div className={styles.ratioBarWrap}>
        <div className={styles.ratioLabels}>
          <span className={styles.askLabel}>
            판매대기 {formatNumber(orderbook.totalAskQuantity)}
          </span>
          <span className={styles.marketLabel}>정규장</span>
          <span className={styles.bidLabel}>
            구매대기 {formatNumber(orderbook.totalBidQuantity)}
          </span>
        </div>
        <div className={styles.ratioBar}>
          <span
            className={styles.ratioAsk}
            style={{ width: `${askPercent}%` }}
            aria-hidden
          />
          <span
            className={styles.ratioBid}
            style={{ width: `${bidPercent}%` }}
            aria-hidden
          />
        </div>
      </div>
    </div>
  );
}
