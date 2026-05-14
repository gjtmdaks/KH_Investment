"use client";

import type { OrderKind, OrderType } from "@/lib/order";
import type { PriceResponse } from "@/lib/stock/stockDetailTypes";
import { formatNumber } from "@/lib/stock/stockDetailFormat";

import styles from "./stockDetail.module.css";

export function StockDetailOrderCard({
  orderKind,
  setOrderKind,
  orderType,
  setOrderType,
  quantity,
  setQuantity,
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
  orderLoading: boolean;
  orderMessage: string | null;
  handleCreateOrder: () => void | Promise<void>;
  price: PriceResponse | null;
}) {
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
          onChange={(event) => setOrderType(event.target.value as OrderType)}
        >
          <option value="MARKET">시장가</option>
          {/* <option value="LIMIT">지정가</option> */}
        </select>
      </label>

      <label>
        {orderKind === "BUY" ? "구매 가격" : "판매 가격"}
        <input value={formatNumber(price?.currentPrice)} readOnly />
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
        onClick={() => void handleCreateOrder()}
        disabled={orderLoading}
      >
        {orderLoading
          ? "처리 중..."
          : orderKind === "BUY"
            ? "구매하기"
            : "판매하기"}
      </button>

      {orderMessage ? (
        <p className={styles.orderMessage}>{orderMessage}</p>
      ) : null}
    </div>
  );
}
