"use client";

import { useEffect, useState } from "react";
import styles from "../myAccount.module.css";
import {
  getOrderHistory,
  cancelOrder,
  updateOrderPrice,
  type OrderHistoryResponse,
} from "@/lib/order";

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
  const [selectedOrder, setSelectedOrder] =
  useState<OrderHistoryResponse | null>(null);
  const [nextPrice, setNextPrice] = useState("");
  const [modalMessage, setModalMessage] = useState<string | null>(null);

  function openManageModal(order: OrderHistoryResponse) {
    setSelectedOrder(order);
    setNextPrice(String(order.price));
    setModalMessage(null);
  }

  function closeManageModal() {
    setSelectedOrder(null);
    setNextPrice("");
    setModalMessage(null);
  }
  async function handleUpdateSelectedOrderPrice() {
  if (!selectedOrder) return;

  const parsedPrice = Number(nextPrice.replaceAll(",", "").trim());

  if (!parsedPrice || parsedPrice <= 0) {
    setModalMessage("변경할 주문 가격을 올바르게 입력해주세요.");
    return;
  }

  try {
    await updateOrderPrice(selectedOrder.orderId, parsedPrice);

    setOrders((prev) =>
      prev.map((order) =>
        order.orderId === selectedOrder.orderId
          ? { ...order, price: parsedPrice }
          : order
      )
    );

    setSelectedOrder((prev) =>
      prev ? { ...prev, price: parsedPrice } : prev
    );

    setModalMessage("주문 가격이 변경되었습니다.");
  } catch (error) {
    console.error(error);
    setModalMessage("주문 가격 변경에 실패했습니다.");
  }
}

async function handleCancelSelectedOrder() {
  if (!selectedOrder) return;

  try {
    await cancelOrder(selectedOrder.orderId);

    setOrders((prev) =>
      prev.filter((order) => order.orderId !== selectedOrder.orderId)
    );

    closeManageModal();
  } catch (error) {
    console.error(error);
    setModalMessage("주문 취소에 실패했습니다.");
  }
}

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
    <>
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
                  <th>관리</th>
                  <th>주문번호</th>
                  <th>구분</th>
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
                    <td>
                      <button
                        type="button"
                        className={styles.manageButton}
                        onClick={() => openManageModal(order)}
                      >
                        관리
                      </button>
                    </td>
                    <td>{order.orderId}</td>
                    <td>{getOrderKindLabel(order.orderKind)}</td>
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
        
      {selectedOrder ? (
        <div className={styles.modalOverlay} onClick={closeManageModal}>
          <div
            className={styles.orderManageModal}
            onClick={(event) => event.stopPropagation()}
          >
            <div className={styles.modalHeader}>
              <div>
                <h3>주문 관리</h3>
                <p>
                  {selectedOrder.stockName} · {selectedOrder.stockCode}
                </p>
              </div>

              <button
                type="button"
                className={styles.modalCloseButton}
                onClick={closeManageModal}
              >
                ×
              </button>
            </div>

            <div className={styles.modalInfoGrid}>
              <div>
                <span>구분</span>
                <strong>{getOrderKindLabel(selectedOrder.orderKind)}</strong>
              </div>

              <div>
                <span>주문수량</span>
                <strong>{selectedOrder.quantity.toLocaleString()}주</strong>
              </div>

              <div>
                <span>현재 주문가격</span>
                <strong>{formatWon(selectedOrder.price)}</strong>
              </div>

              <div>
                <span>상태</span>
                <strong>{getStatusLabel(selectedOrder.status)}</strong>
              </div>
            </div>

            <label className={styles.modalLabel}>
              변경할 지정가
              <input
                value={nextPrice}
                onChange={(event) => setNextPrice(event.target.value)}
                placeholder="변경할 가격 입력"
                inputMode="numeric"
              />
            </label>

            {modalMessage ? (
              <p className={styles.modalMessage}>{modalMessage}</p>
            ) : null}

            <div className={styles.modalActions}>
              <button
                type="button"
                className={styles.editButton}
                onClick={handleUpdateSelectedOrderPrice}
              >
                지정가 변경
              </button>

              <button
                type="button"
                className={styles.cancelButton}
                onClick={handleCancelSelectedOrder}
              >
                주문 취소
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </>
  );
}