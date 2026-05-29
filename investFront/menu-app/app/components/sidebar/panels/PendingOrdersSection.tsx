"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

import {
  cancelOrder,
  getPendingOrders,
  type PendingOrderResponse,
} from "@/lib/order";

import styles from "./myInvestmentPanel.module.css";

function formatPrice(value: number) {
  return value.toLocaleString("ko-KR");
}

function formatDate(value: string) {
  if (!value) return "-";

  return new Date(value).toLocaleString("ko-KR", {
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function getOrderKindText(orderKind: PendingOrderResponse["orderKind"]) {
  return orderKind === "BUY" ? "매수" : "매도";
}

function getOrderTypeText(orderType: PendingOrderResponse["orderType"]) {
  return orderType === "LIMIT" ? "지정가" : "시장가";
}

export function PendingOrdersSection() {
  const router = useRouter();

  const [orders, setOrders] = useState<PendingOrderResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [cancelingOrderId, setCancelingOrderId] = useState<number | null>(null);

  async function loadPendingOrders() {
    try {
      setLoading(true);

      const result = await getPendingOrders();

      setOrders(Array.isArray(result) ? result : []);
    } catch (error) {
      console.error(error);
      setOrders([]);
    } finally {
      setLoading(false);
    }
  }

  async function handleCancelOrder(orderId: number) {
    if (!confirm("예약 주문을 취소하시겠습니까?")) {
      return;
    }

    try {
      setCancelingOrderId(orderId);

      await cancelOrder(orderId);
      await loadPendingOrders();
    } catch (error) {
      console.error(error);
      alert("예약 주문 취소에 실패했습니다.");
    } finally {
      setCancelingOrderId(null);
    }
  }

  useEffect(() => {
    loadPendingOrders();
  }, []);

  return (
    <section className={styles.pendingSection}>
      <div className={styles.pendingHeader}>
        <div>
          <h3 className={styles.pendingTitle}>예약 주문</h3>
          <p className={styles.pendingSubText}>지정가 대기 주문</p>
        </div>

        <button
          type="button"
          className={styles.pendingRefreshButton}
          onClick={loadPendingOrders}
          disabled={loading}
        >
          {loading ? "조회 중" : "새로고침"}
        </button>
      </div>

      {orders.length === 0 ? (
        <div className={styles.pendingEmpty}>
          {loading ? "예약 주문을 불러오는 중입니다." : "예약 주문이 없습니다."}
        </div>
      ) : (
        <div className={styles.pendingList}>
          {orders.map((order) => {
            const totalAmount = order.price * order.quantity;

            return (
              <article
                key={order.orderId}
                className={styles.pendingItem}
                onClick={() => router.push(`/main/stock/${order.stockCode}`)}
                role="button"
                tabIndex={0}
                onKeyDown={(event) => {
                  if (event.key === "Enter") {
                    router.push(`/main/stock/${order.stockCode}`);
                  }
                }}
              >
                <div className={styles.pendingItemTop}>
                  <div>
                    <strong className={styles.pendingStockName}>
                      {order.stockName || order.stockCode}
                    </strong>
                    <p className={styles.pendingStockCode}>{order.stockCode}</p>
                  </div>

                  <span
                    className={`${styles.pendingKindBadge} ${
                      order.orderKind === "BUY"
                        ? styles.pendingBuy
                        : styles.pendingSell
                    }`}
                  >
                    {getOrderKindText(order.orderKind)}
                  </span>
                </div>

                <div className={styles.pendingInfoGrid}>
                  <div>
                    <span>유형</span>
                    <strong>{getOrderTypeText(order.orderType)}</strong>
                  </div>

                  <div>
                    <span>가격</span>
                    <strong>{formatPrice(order.price)}원</strong>
                  </div>

                  <div>
                    <span>수량</span>
                    <strong>{order.quantity}주</strong>
                  </div>

                  <div>
                    <span>총액</span>
                    <strong>{formatPrice(totalAmount)}원</strong>
                  </div>
                </div>

                <div className={styles.pendingBottom}>
                  <span>{formatDate(order.createdAt)}</span>

                  <button
                    type="button"
                    className={styles.pendingCancelButton}
                    onClick={(event) => {
                      event.stopPropagation();
                      handleCancelOrder(order.orderId);
                    }}
                    disabled={cancelingOrderId === order.orderId}
                  >
                    {cancelingOrderId === order.orderId ? "취소 중" : "취소"}
                  </button>
                </div>
              </article>
            );
          })}
        </div>
      )}
    </section>
  );
}