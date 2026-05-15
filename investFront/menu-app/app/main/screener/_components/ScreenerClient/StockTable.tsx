"use client";

import { useRouter } from "next/navigation";

import styles from "../../ScreenerPage.module.css";

import { StockItem } from "../types";

export default function StockTable({
  stocks,
}: {
  stocks: StockItem[];
}) {
  const router = useRouter();

  return (
    <section className={styles.tableSection}>
      <table className={styles.table}>
        <thead>
          <tr>
            <th>종목명</th>
            <th>시장</th>
            <th>업종</th>
            <th>현재가</th>
            <th>등락률</th>
            <th>거래량</th>
          </tr>
        </thead>

        <tbody>
          {stocks.map((stock) => (
            <tr
              key={stock.stockCode}
              className={styles.clickableRow}
              onClick={() =>
                router.push(
                  `/main/stock/${stock.stockCode}`
                )
              }
            >
              <td>{stock.stockName}</td>

              <td>{stock.marketType}</td>

              <td>{stock.sector}</td>

              <td>
                {stock.currentPrice?.toLocaleString()}원
              </td>

              <td
                className={
                  stock.changeRate >= 0
                    ? styles.upRate
                    : styles.downRate
                }
              >
                {stock.changeRate}%
              </td>

              <td>
                {stock.volume?.toLocaleString()}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}