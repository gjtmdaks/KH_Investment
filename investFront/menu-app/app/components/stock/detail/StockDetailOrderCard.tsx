"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";

import { useAuth } from "@/app/context/AuthContext";
import { getAccountAssets, type HoldingStock } from "@/lib/account";
import type { OrderKind, OrderType } from "@/lib/order";
import {
  snapPriceToTick,
  stepPriceByTick,
} from "@/lib/stock/koreanStockPriceTick";
import { formatNumber, formatWon, parseNumeric } from "@/lib/stock/stockDetailFormat";
import type { PriceResponse } from "@/lib/stock/stockDetailTypes";

import styles from "./css/stockDetailOrderCard.module.css";

function getBuyOrderButtonLabel(
  isAuthenticated: boolean,
  orderLoading: boolean,
  orderType: OrderType
): string {
  if (orderLoading) {
    return "처리 중...";
  }

  const actionLabel =
    orderType === "LIMIT" ? "구매 예약하기" : "구매하기";

  if (!isAuthenticated) {
    return `로그인하고 ${actionLabel}`;
  }

  return actionLabel;
}

function getSellOrderButtonLabel(
  isAuthenticated: boolean,
  orderLoading: boolean,
  orderType: OrderType,
  totalOrderAmount: number
): string {
  if (orderLoading) {
    return "처리 중...";
  }

  if (!isAuthenticated) {
    return "로그인하고 판매하기";
  }

  const actionLabel = orderType === "LIMIT" ? "판매 예약하기" : "판매하기";

  if (totalOrderAmount > 0) {
    return `${formatNumber(String(totalOrderAmount))}원 ${actionLabel}`;
  }

  return actionLabel;
}

function formatSignedWon(value: number) {
  const rounded = Math.round(value);
  const sign = rounded > 0 ? "+" : "";

  return `${sign}${rounded.toLocaleString("ko-KR")}원`;
}

function formatSignedPercent(value: number) {
  const sign = value > 0 ? "+" : "";

  return `${sign}${value.toFixed(2)}%`;
}

function formatInputDigits(value: string): string {
  const digits = value.replace(/\D/g, "");

  if (!digits) {
    return "";
  }

  return Number(digits).toLocaleString("ko-KR");
}

function getProfitToneClass(value: number) {
  if (value > 0) {
    return styles.profitUp;
  }

  if (value < 0) {
    return styles.profitDown;
  }

  return styles.profitFlat;
}

function OrderStepper({
  onMinus,
  onPlus,
  disabled,
}: {
  onMinus: () => void;
  onPlus: () => void;
  disabled?: boolean;
}) {
  return (
    <div className={styles.stepper}>
      <button type="button" onClick={onMinus} disabled={disabled} aria-label="감소">
        −
      </button>
      <span className={styles.stepperDivider} aria-hidden />
      <button type="button" onClick={onPlus} disabled={disabled} aria-label="증가">
        +
      </button>
    </div>
  );
}

function SellEmptyState() {
  return (
    <div className={styles.sellEmptyState}>
      <div className={styles.sellEmptyIcon} aria-hidden>
        📄
      </div>
      <p className={styles.sellEmptyText}>판매할 주식이 없어요.</p>
    </div>
  );
}

export function StockDetailOrderCard({
  stockCode,
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
  price,
}: {
  stockCode: string;
  orderKind: OrderKind;
  setOrderKind: (value: OrderKind) => void;
  orderType: OrderType;
  setOrderType: (value: OrderType) => void;
  quantity: string;
  setQuantity: (value: string) => void;
  orderPrice: string;
  setOrderPrice: (value: string) => void;
  orderLoading: boolean;
  orderMessage: string | null;
  handleCreateOrder: () => void | Promise<void>;
  price: PriceResponse | null;
}) {
  const router = useRouter();
  const { isAuthenticated } = useAuth();
  const [availableCash, setAvailableCash] = useState(0);
  const [holding, setHolding] = useState<HoldingStock | null>(null);
  const [assetsLoaded, setAssetsLoaded] = useState(false);

  const currentPrice = useMemo(
    () => parseNumeric(price?.currentPrice) ?? 0,
    [price?.currentPrice]
  );

  const unitPrice = useMemo(() => {
    if (orderType === "MARKET") {
      return currentPrice;
    }

    return parseNumeric(orderPrice) ?? 0;
  }, [currentPrice, orderPrice, orderType]);

  const orderQuantity = useMemo(() => parseNumeric(quantity) ?? 0, [quantity]);

  const totalOrderAmount = useMemo(() => {
    if (unitPrice <= 0 || orderQuantity <= 0) {
      return 0;
    }

    return unitPrice * orderQuantity;
  }, [orderQuantity, unitPrice]);

  const sellableQuantity = holding?.quantity ?? 0;
  const isSellMode = orderKind === "SELL";
  const showSellEmpty =
    isSellMode && isAuthenticated && assetsLoaded && sellableQuantity <= 0;

  const maxSellAmount = useMemo(() => {
    if (!isSellMode || currentPrice <= 0 || sellableQuantity <= 0) {
      return 0;
    }

    return currentPrice * sellableQuantity;
  }, [currentPrice, isSellMode, sellableQuantity]);

  const holdingInsight = useMemo(() => {
    if (!holding || holding.quantity <= 0) {
      return null;
    }

    const holdingQuantity = holding.quantity;
    const avgPrice = holding.avgPrice;
    const markPrice = currentPrice > 0 ? currentPrice : holding.currentPrice;
    const investedAmount = avgPrice * holdingQuantity;
    const profitAmount = Math.round(markPrice * holdingQuantity - investedAmount);
    const profitRate =
      investedAmount > 0 ? (profitAmount / investedAmount) * 100 : 0;

    let expectedAvgPrice: number | null = null;
    let expectedSellProfit: number | null = null;
    let expectedSellProfitRate: number | null = null;

    if (orderKind === "BUY" && orderQuantity > 0 && unitPrice > 0) {
      expectedAvgPrice = Math.round(
        (investedAmount + unitPrice * orderQuantity) /
          (holdingQuantity + orderQuantity)
      );
    }

    if (orderKind === "SELL" && orderQuantity > 0 && unitPrice > 0) {
      expectedSellProfit = Math.round(orderQuantity * (unitPrice - avgPrice));
      expectedSellProfitRate =
        avgPrice > 0 ? ((unitPrice - avgPrice) / avgPrice) * 100 : 0;
    }

    return {
      avgPrice,
      expectedAvgPrice,
      profitAmount,
      profitRate,
      expectedSellProfit,
      expectedSellProfitRate,
    };
  }, [currentPrice, holding, orderKind, orderQuantity, unitPrice]);

  const showBuyHoldingInsight = orderKind === "BUY" && holdingInsight !== null;
  const showSellHoldingInsight = orderKind === "SELL" && holdingInsight !== null;

  useEffect(() => {
    if (!isAuthenticated) {
      setAvailableCash(0);
      setHolding(null);
      setAssetsLoaded(true);
      return;
    }

    let cancelled = false;

    async function loadAccount() {
      try {
        const assets = await getAccountAssets();
        if (cancelled) {
          return;
        }

        setAvailableCash(Math.max(0, Math.floor(assets.availableCash ?? 0)));

        const matchedHolding = assets.holdings?.find(
          (item) => item.stockCode === stockCode
        );

        if (matchedHolding && matchedHolding.quantity > 0) {
          setHolding(matchedHolding);
        } else {
          setHolding(null);
        }
      } catch (error) {
        console.error(error);
        if (!cancelled) {
          setAvailableCash(0);
          setHolding(null);
        }
      } finally {
        if (!cancelled) {
          setAssetsLoaded(true);
        }
      }
    }

    setAssetsLoaded(false);
    void loadAccount();

    return () => {
      cancelled = true;
    };
  }, [isAuthenticated, stockCode, orderMessage]);

  const applyLimitPrice = useCallback(
    (nextPrice: number) => {
      if (nextPrice <= 0) {
        setOrderPrice("");
        return;
      }

      setOrderPrice(formatInputDigits(String(Math.round(nextPrice))));
    },
    [setOrderPrice]
  );

  const handleOrderTypeChange = useCallback(
    (nextOrderType: OrderType) => {
      setOrderType(nextOrderType);

      if (nextOrderType === "LIMIT" && currentPrice > 0) {
        applyLimitPrice(snapPriceToTick(currentPrice));
      }
    },
    [applyLimitPrice, currentPrice, setOrderType]
  );

  const handlePriceMinus = useCallback(() => {
    if (orderType !== "LIMIT") {
      return;
    }

    const base =
      parseNumeric(orderPrice) ?? (currentPrice > 0 ? snapPriceToTick(currentPrice) : 0);

    if (base <= 0) {
      return;
    }

    applyLimitPrice(stepPriceByTick(base, -1));
  }, [applyLimitPrice, currentPrice, orderPrice, orderType]);

  const handlePricePlus = useCallback(() => {
    if (orderType !== "LIMIT") {
      return;
    }

    const base =
      parseNumeric(orderPrice) ?? (currentPrice > 0 ? snapPriceToTick(currentPrice) : 0);

    if (base <= 0) {
      if (currentPrice > 0) {
        applyLimitPrice(snapPriceToTick(currentPrice));
      }
      return;
    }

    applyLimitPrice(stepPriceByTick(base, 1));
  }, [applyLimitPrice, currentPrice, orderPrice, orderType]);

  const handleQuantityMinus = useCallback(() => {
    const current = parseNumeric(quantity) ?? 0;
    const next = Math.max(0, current - 1);

    setQuantity(next > 0 ? String(next) : "");
  }, [quantity, setQuantity]);

  const handleQuantityPlus = useCallback(() => {
    const current = parseNumeric(quantity) ?? 0;
    const next =
      isSellMode && sellableQuantity > 0
        ? Math.min(sellableQuantity, current + 1)
        : current + 1;

    setQuantity(String(next));
  }, [isSellMode, quantity, sellableQuantity, setQuantity]);

  const applyQuantityRatio = useCallback(
    (ratio: number) => {
      if (orderKind === "BUY") {
        if (unitPrice <= 0) {
          return;
        }

        const budget = Math.floor(availableCash * ratio);
        const nextQuantity = Math.floor(budget / unitPrice);
        setQuantity(nextQuantity > 0 ? String(nextQuantity) : "");
        return;
      }

      const nextQuantity = Math.floor(sellableQuantity * ratio);
      setQuantity(nextQuantity > 0 ? String(nextQuantity) : "");
    },
    [availableCash, orderKind, sellableQuantity, setQuantity, unitPrice]
  );

  const handleQuantityChange = useCallback(
    (rawValue: string) => {
      const digits = rawValue.replace(/\D/g, "");

      if (!digits) {
        setQuantity("");
        return;
      }

      let next = Number(digits);

      if (isSellMode && sellableQuantity > 0) {
        next = Math.min(sellableQuantity, next);
      }

      setQuantity(String(next));
    },
    [isSellMode, sellableQuantity, setQuantity]
  );

  function handleOrderButtonClick() {
    if (!isAuthenticated) {
      router.push("/sign-in");
      return;
    }

    void handleCreateOrder();
  }

  const buyPriceFieldValue =
    orderType === "MARKET"
      ? currentPrice > 0
        ? formatNumber(String(currentPrice))
        : ""
      : orderPrice;

  const sellPriceFieldValue = orderType === "MARKET" ? "" : orderPrice;

  const sellTotalDisplay =
    totalOrderAmount > 0
      ? formatNumber(String(totalOrderAmount))
      : maxSellAmount > 0
        ? `최대 ${formatNumber(String(maxSellAmount))}`
        : "";

  return (
    <div className={styles.orderCard}>
      <div className={styles.orderTabs}>
        <button
          type="button"
          className={orderKind === "BUY" ? styles.buyTab : ""}
          onClick={() => setOrderKind("BUY")}
        >
          구매
        </button>

        <button
          type="button"
          className={orderKind === "SELL" ? styles.sellTab : ""}
          onClick={() => setOrderKind("SELL")}
        >
          판매
        </button>
      </div>

      {showSellEmpty ? (
        <SellEmptyState />
      ) : (
        <>
          <div className={styles.fieldBlock}>
            <div className={styles.fieldHeader}>
              <span className={styles.fieldLabel}>
                {isSellMode ? "판매 가격" : "구매 가격"}
              </span>
              <div className={styles.orderTypeToggle}>
                <button
                  type="button"
                  className={orderType === "LIMIT" ? styles.orderTypeActive : ""}
                  onClick={() => handleOrderTypeChange("LIMIT")}
                >
                  지정가
                </button>
                <button
                  type="button"
                  className={orderType === "MARKET" ? styles.orderTypeActive : ""}
                  onClick={() => handleOrderTypeChange("MARKET")}
                >
                  시장가
                </button>
              </div>
            </div>

            <div className={styles.inputRow}>
              <div className={styles.inputWrap}>
                <input
                  value={isSellMode ? sellPriceFieldValue : buyPriceFieldValue}
                  onChange={(event) => {
                    if (orderType === "MARKET") {
                      return;
                    }

                    setOrderPrice(formatInputDigits(event.target.value));
                  }}
                  readOnly={orderType === "MARKET"}
                  placeholder={
                    isSellMode && orderType === "MARKET"
                      ? "최대한 빠른가격"
                      : "가격 입력"
                  }
                  className={
                    isSellMode && orderType === "MARKET"
                      ? styles.marketPricePlaceholder
                      : undefined
                  }
                  inputMode="numeric"
                />
                {!(isSellMode && orderType === "MARKET") ? (
                  <span className={styles.inputSuffix}>원</span>
                ) : null}
              </div>
              <OrderStepper
                onMinus={handlePriceMinus}
                onPlus={handlePricePlus}
                disabled={orderType === "MARKET"}
              />
            </div>
          </div>

          <div className={styles.fieldBlock}>
            <span className={styles.fieldLabel}>수량</span>
            <div className={styles.inputRow}>
              <div className={styles.inputWrap}>
                <input
                  value={quantity}
                  onChange={(event) => {
                    if (isSellMode) {
                      handleQuantityChange(event.target.value);
                      return;
                    }

                    setQuantity(formatInputDigits(event.target.value));
                  }}
                  placeholder={
                    isSellMode && sellableQuantity > 0
                      ? `최대 ${formatNumber(String(sellableQuantity))}주 가능`
                      : "수량 입력"
                  }
                  inputMode="numeric"
                />
                {isSellMode ? (
                  <span className={styles.inputSuffix}>주</span>
                ) : null}
              </div>
              <OrderStepper onMinus={handleQuantityMinus} onPlus={handleQuantityPlus} />
            </div>

            <div className={styles.percentRow}>
              <button type="button" onClick={() => applyQuantityRatio(0.1)}>
                10%
              </button>
              <button type="button" onClick={() => applyQuantityRatio(0.25)}>
                25%
              </button>
              <button type="button" onClick={() => applyQuantityRatio(0.5)}>
                50%
              </button>
              <button type="button" onClick={() => applyQuantityRatio(1)}>
                최대
              </button>
            </div>
          </div>

          {isSellMode ? (
            <div className={styles.sellTotalRow}>
              <span className={styles.fieldLabel}>예상 총 주문 금액</span>
              <div className={`${styles.inputWrap} ${styles.sellTotalWrap}`}>
                <div
                  className={`${styles.sellTotalField} ${
                    sellTotalDisplay ? "" : styles.sellTotalFieldEmpty
                  }`}
                  aria-live="polite"
                >
                  {sellTotalDisplay || "금액 입력"}
                </div>
                {sellTotalDisplay ? (
                  <span className={styles.inputSuffix}>원</span>
                ) : null}
              </div>
            </div>
          ) : (
            <div className={styles.summaryBlock}>
              <p className={styles.totalAmount}>
                총 주문 금액 {formatWon(String(totalOrderAmount))}
              </p>
              <p className={styles.availableCash}>
                주문 가능 금액 {formatWon(String(availableCash))}
              </p>
            </div>
          )}

          {showBuyHoldingInsight && holdingInsight ? (
            <div className={styles.holdingPanel}>
              <div className={styles.holdingRow}>
                <span className={styles.holdingLabel}>내 주식 평단가</span>
                <span className={styles.holdingValue}>
                  {formatWon(String(Math.round(holdingInsight.avgPrice)))}
                </span>
              </div>
              <div className={styles.holdingRow}>
                <span className={styles.holdingLabel}>구매 후 예상 평단가</span>
                <span className={styles.holdingValue}>
                  {holdingInsight.expectedAvgPrice !== null
                    ? formatWon(String(holdingInsight.expectedAvgPrice))
                    : "-"}
                </span>
              </div>
              <div className={styles.holdingRow}>
                <span className={styles.holdingLabel}>현재 수익</span>
                <span
                  className={`${styles.holdingValue} ${getProfitToneClass(
                    holdingInsight.profitAmount
                  )}`}
                >
                  {formatSignedWon(holdingInsight.profitAmount)} (
                  {formatSignedPercent(holdingInsight.profitRate)})
                </span>
              </div>
            </div>
          ) : null}

          {showSellHoldingInsight && holdingInsight ? (
            <div className={styles.holdingPanel}>
              <div className={styles.holdingRow}>
                <span className={styles.holdingLabel}>내 주식 평단가</span>
                <span className={styles.holdingValue}>
                  {formatWon(String(Math.round(holdingInsight.avgPrice)))}
                </span>
              </div>
              <div className={styles.holdingRow}>
                <span className={styles.holdingLabel}>현재 수익</span>
                <span
                  className={`${styles.holdingValue} ${getProfitToneClass(
                    holdingInsight.profitAmount
                  )}`}
                >
                  {formatSignedWon(holdingInsight.profitAmount)} (
                  {formatSignedPercent(holdingInsight.profitRate)})
                </span>
              </div>
              <div className={styles.holdingRow}>
                <span className={styles.holdingLabel}>예상 수익률</span>
                <span
                  className={`${styles.holdingValue} ${
                    holdingInsight.expectedSellProfitRate !== null
                      ? getProfitToneClass(holdingInsight.expectedSellProfitRate)
                      : styles.profitFlat
                  }`}
                >
                  {holdingInsight.expectedSellProfitRate !== null
                    ? formatSignedPercent(holdingInsight.expectedSellProfitRate)
                    : "-"}
                </span>
              </div>
              <div className={styles.holdingRow}>
                <span className={styles.holdingLabel}>예상 손익</span>
                <span
                  className={`${styles.holdingValue} ${
                    holdingInsight.expectedSellProfit !== null
                      ? getProfitToneClass(holdingInsight.expectedSellProfit)
                      : styles.profitFlat
                  }`}
                >
                  {holdingInsight.expectedSellProfit !== null
                    ? formatSignedWon(holdingInsight.expectedSellProfit)
                    : "-"}
                </span>
              </div>
            </div>
          ) : null}

          <button
            type="button"
            className={orderKind === "BUY" ? styles.buyButton : styles.sellButton}
            onClick={handleOrderButtonClick}
            disabled={orderLoading || (isSellMode && sellableQuantity <= 0)}
          >
            {isSellMode
              ? getSellOrderButtonLabel(
                  isAuthenticated,
                  orderLoading,
                  orderType,
                  totalOrderAmount
                )
              : getBuyOrderButtonLabel(isAuthenticated, orderLoading, orderType)}
          </button>
        </>
      )}

      {orderMessage ? (
        <p className={styles.orderMessage}>{orderMessage}</p>
      ) : null}
    </div>
  );
}
