"use client";

import { useRouter } from "next/navigation";

import { useAuth } from "@/app/context/AuthContext";
import type { OrderKind, OrderType } from "@/lib/order";
import type { PriceResponse } from "@/lib/stock/stockDetailTypes";
import { formatNumber } from "@/lib/stock/stockDetailFormat";

import styles from "./css/stockDetailOrderCard.module.css";

function getOrderButtonLabel(
  isAuthenticated: boolean,
  orderLoading: boolean,
  orderKind: OrderKind,
  orderType: OrderType
): string {
  if (orderLoading) {
    return "처리 중...";
  }

  const actionLabel =
    orderType === "LIMIT"
      ? orderKind === "BUY"
        ? "구매 예약하기"
        : "판매 예약하기"
      : orderKind === "BUY"
        ? "구매하기"
        : "판매하기";

  if (!isAuthenticated) {
    return `로그인하고 ${actionLabel}`;
  }

  return actionLabel;
}

export function StockDetailOrderCard({
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

  function handleOrderButtonClick() {
    if (!isAuthenticated) {
      router.push("/sign-in");
      return;
    }

    void handleCreateOrder();
  }

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

      <label>
        주문 유형
        <select
          value={orderType}
          onChange={(event) => {
            const nextOrderType = event.target.value as OrderType;

            setOrderType(nextOrderType);

            if (nextOrderType === "LIMIT") {
              setOrderPrice(String(price?.currentPrice ?? ""));
            } else {
              setOrderPrice("");
            }
          }}
        >
          <option value="MARKET">시장가</option>
          <option value="LIMIT">지정가</option>
        </select>
      </label>

      <label>
        {orderType === "MARKET"
          ? "예상 체결가"
          : orderKind === "BUY"
            ? "구매 예약 가격"
            : "판매 예약 가격"}

        <input
          value={
            orderType === "MARKET"
              ? formatNumber(price?.currentPrice)
              : orderPrice
          }
          onChange={(event) => setOrderPrice(event.target.value)}
          readOnly={orderType === "MARKET"}
          placeholder="가격 입력"
          inputMode="numeric"
        />
      </label>

      <label>
        수량
        <input
          value={quantity}
          onChange={(event) => setQuantity(event.target.value)}
          placeholder="수량 입력"
          inputMode="numeric"
        />
      </label>

      <button
        type="button"
        className={orderKind === "BUY" ? styles.buyButton : styles.sellButton}
        onClick={handleOrderButtonClick}
        disabled={orderLoading}
      >
        {getOrderButtonLabel(
          isAuthenticated,
          orderLoading,
          orderKind,
          orderType
        )}
      </button>

      {orderMessage ? (
        <p className={styles.orderMessage}>{orderMessage}</p>
      ) : null}
    </div>
  );
}