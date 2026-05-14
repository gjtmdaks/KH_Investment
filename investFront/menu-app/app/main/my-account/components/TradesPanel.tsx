"use client";

import { useEffect, useState } from "react";
import styles from "../myAccount.module.css";
import { getTradeHistory, type TradeResponse } from "@/lib/order";

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

function getOrderKindClass(orderKind: string) {
  if (orderKind === "BUY") return styles.buy;
  if (orderKind === "SELL") return styles.sell;
  return "";
}

export default function TradesPanel() {
  const [trades, setTrades] = useState<TradeResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    async function fetchTradeHistory() {
      try {
        setLoading(true);
        setErrorMessage(null);

        const data = await getTradeHistory();

        setTrades(data);
      } catch (error) {
        console.error(error);
        setErrorMessage("거래내역을 불러오지 못했습니다.");
      } finally {
        setLoading(false);
      }
    }

    fetchTradeHistory();
  }, []);

  return (
    <section className={styles.card}>
      <div className={styles.cardHeader}>
        <div>
          <h2>거래내역</h2>
          <p>체결 완료된 거래 내역입니다.</p>
        </div>
      </div>

      {loading ? (
        <div className={styles.infoList}>
          <div className={styles.infoRow}>
            <span>상태</span>
            <strong>거래내역을 불러오는 중입니다.</strong>
          </div>
        </div>
      ) : errorMessage ? (
        <div className={styles.infoList}>
          <div className={styles.infoRow}>
            <span>오류</span>
            <strong>{errorMessage}</strong>
          </div>
        </div>
      ) : trades.length === 0 ? (
        <div className={styles.infoList}>
          <div className={styles.infoRow}>
            <span>거래내역</span>
            <strong>아직 체결된 거래가 없습니다.</strong>
          </div>
        </div>
      ) : (
        <div className={styles.tableWrap}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>체결번호</th>
                <th>구분</th>
                <th>종목명</th>
                <th>종목코드</th>
                <th>체결가</th>
                <th>체결수량</th>
                <th>체결금액</th>
                <th>체결시간</th>
              </tr>
            </thead>

            <tbody>
              {trades.map((trade) => (
                <tr key={trade.tradeId}>
                  <td>{trade.tradeId}</td>
                  <td>
                    <span
                      className={`${styles.badge} ${getOrderKindClass(
                        trade.orderKind
                      )}`}
                    >
                      {getOrderKindLabel(trade.orderKind)}
                    </span>
                  </td>
                  <td>{trade.stockName}</td>
                  <td>{trade.stockCode}</td>
                  <td>{formatWon(trade.price)}</td>
                  <td>{trade.quantity.toLocaleString()}주</td>
                  <td>{formatWon(trade.price * trade.quantity)}</td>
                  <td>{formatDate(trade.executedAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}