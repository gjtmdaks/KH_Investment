"use client";

import { useEffect, useState } from "react";
import styles from "../myAccount.module.css";
import { getOrderHistory, type OrderHistoryResponse } from "@/lib/order";

function formatWon(value: number) {
  return `${value.toLocaleString()}원`;
}

function formatDate(value: string) {
  if (!value) return "-";

  return new Date(value).toLocaleString("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function getOrderKindLabel(orderKind: string) {
  if (orderKind === "BUY") return "매수";
  if (orderKind === "SELL") return "매도";
  return orderKind;
}

function getOrderTypeLabel(orderType: string) {
  if (orderType === "MARKET") return "시장가";
  if (orderType === "LIMIT") return "지정가";
  return orderType;
}

function getStatusLabel(status: string) {
  if (status === "PENDING") return "예약";
  if (status === "FILLED") return "체결";
  if (status === "CANCELED") return "취소";
  if (status === "REJECTED") return "거부";
  return status;
}

export default function OrdersPanel() {
  const [orders, setOrders] = useState<OrderHistoryResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    async function fetchOrderHistory() {
      try {
        setLoading(true);
        setErrorMessage(null);

        const data = await getOrderHistory();

        setOrders(Array.isArray(data) ? data : []);
      } catch (error) {
        console.error(error);
        setErrorMessage("주문내역을 불러오지 못했습니다.");
      } finally {
        setLoading(false);
      }
    }

    fetchOrderHistory();
  }, []);

  return (
    <section className={styles.card}>
      <div className={styles.cardHeader}>
        <div>
          <h2>주문내역</h2>
          <p>요청한 주문 상태를 확인할 수 있습니다.</p>
        </div>
      </div>

      {loading ? (
        <div className={styles.infoList}>
          <div className={styles.infoRow}>
            <span>상태</span>
            <strong>주문내역을 불러오는 중입니다.</strong>
          </div>
        </div>
      ) : errorMessage ? (
        <div className={styles.infoList}>
          <div className={styles.infoRow}>
            <span>오류</span>
            <strong>{errorMessage}</strong>
          </div>
        </div>
      ) : orders.length === 0 ? (
        <div className={styles.infoList}>
          <div className={styles.infoRow}>
            <span>주문내역</span>
            <strong>아직 주문내역이 없습니다.</strong>
          </div>
        </div>
      ) : (
        <div className={styles.tableWrap}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>주문번호</th>
                <th>구분</th>
                <th>유형</th>
                <th>종목명</th>
                <th>종목코드</th>
                <th>주문가격</th>
                <th>주문수량</th>
                <th>상태</th>
                <th>주문시간</th>
              </tr>
            </thead>

            <tbody>
              {orders.map((order) => (
                <tr key={order.orderId}>
                  <td>{order.orderId}</td>
                  <td>{getOrderKindLabel(order.orderKind)}</td>
                  <td>{getOrderTypeLabel(order.orderType)}</td>
                  <td>{order.stockName}</td>
                  <td>{order.stockCode}</td>
                  <td>{formatWon(order.price)}</td>
                  <td>{order.quantity.toLocaleString()}주</td>
                  <td>{getStatusLabel(order.status)}</td>
                  <td>{formatDate(order.createdAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}