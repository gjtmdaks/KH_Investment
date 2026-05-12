"use client";

import styles from "../myAccount.module.css";

type TradeHistory = {
  tradeId: number;
  stockName: string;
  price: number;
  quantity: number;
  executedAt: string;
};

const mockTrades: TradeHistory[] = [
  {
    tradeId: 1,
    stockName: "삼성전자",
    price: 73500,
    quantity: 3,
    executedAt: "2026-05-12 10:35",
  },
  {
    tradeId: 2,
    stockName: "SK하이닉스",
    price: 159000,
    quantity: 1,
    executedAt: "2026-05-12 11:12",
  },
];

function formatWon(value: number) {
  return `${value.toLocaleString()}원`;
}

export default function TradesPanel() {
  return (
    <section className={styles.card}>
      <div className={styles.cardHeader}>
        <div>
          <h2>거래내역</h2>
          <p>체결 완료된 거래 내역입니다.</p>
        </div>
      </div>

      <div className={styles.tableWrap}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>체결번호</th>
              <th>종목명</th>
              <th>체결금액</th>
              <th>체결수량</th>
              <th>체결시간</th>
            </tr>
          </thead>
          <tbody>
            {mockTrades.map((trade) => (
              <tr key={trade.tradeId}>
                <td>{trade.tradeId}</td>
                <td>{trade.stockName}</td>
                <td>{formatWon(trade.price)}</td>
                <td>{trade.quantity.toLocaleString()}주</td>
                <td>{trade.executedAt}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}