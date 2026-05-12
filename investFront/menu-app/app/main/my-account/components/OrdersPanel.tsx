"use client";

import styles from "../myAccount.module.css";

type OrderHistory = {
  orderId: number;
  stockName: string;
  orderKind: "BUY" | "SELL";
  orderType: "MARKET" | "LIMIT";
  price: number;
  quantity: number;
  status: string;
  createdAt: string;
};

const mockOrders: OrderHistory[] = [
  {
    orderId: 1,
    stockName: "삼성전자",
    orderKind: "BUY",
    orderType: "LIMIT",
    price: 74000,
    quantity: 5,
    status: "PENDING",
    createdAt: "2026-05-12 10:30",
  },
  {
    orderId: 2,
    stockName: "SK하이닉스",
    orderKind: "SELL",
    orderType: "MARKET",
    price: 159000,
    quantity: 1,
    status: "FILLED",
    createdAt: "2026-05-12 11:10",
  },
];

function formatWon(value: number) {
  return `${value.toLocaleString()}원`;
}

function getOrderKindName(kind: "BUY" | "SELL") {
  return kind === "BUY" ? "매수" : "매도";
}

function getOrderTypeName(type: "MARKET" | "LIMIT") {
  return type === "MARKET" ? "시장가" : "지정가";
}

function getStatusName(status: string) {
  switch (status) {
    case "PENDING":
      return "주문 대기";
    case "PARTIAL":
      return "일부 체결";
    case "FILLED":
      return "체결 완료";
    case "CANCELED":
      return "취소";
    case "REJECTED":
      return "거부";
    default:
      return status;
  }
}

export default function OrdersPanel() {
  return (
    <section className={styles.card}>
      <div className={styles.cardHeader}>
        <div>
          <h2>주문내역</h2>
          <p>요청한 주문과 현재 주문 상태입니다.</p>
        </div>
      </div>

      <div className={styles.tableWrap}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>주문번호</th>
              <th>종목명</th>
              <th>매매구분</th>
              <th>주문유형</th>
              <th>가격</th>
              <th>수량</th>
              <th>상태</th>
              <th>주문시각</th>
            </tr>
          </thead>
          <tbody>
            {mockOrders.map((order) => (
              <tr key={order.orderId}>
                <td>{order.orderId}</td>
                <td>{order.stockName}</td>
                <td>
                  <span
                    className={`${styles.badge} ${
                      order.orderKind === "BUY" ? styles.buy : styles.sell
                    }`}
                  >
                    {getOrderKindName(order.orderKind)}
                  </span>
                </td>
                <td>{getOrderTypeName(order.orderType)}</td>
                <td>{formatWon(order.price)}</td>
                <td>{order.quantity.toLocaleString()}주</td>
                <td>{getStatusName(order.status)}</td>
                <td>{order.createdAt}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}