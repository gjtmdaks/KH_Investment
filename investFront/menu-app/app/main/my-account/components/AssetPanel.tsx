"use client";

import styles from "../myAccount.module.css";

type HoldingStock = {
  stockCode: string;
  stockName: string;
  quantity: number;
  avgPrice: number;
  currentPrice: number;
};

const mockHoldings: HoldingStock[] = [
  {
    stockCode: "005930",
    stockName: "삼성전자",
    quantity: 10,
    avgPrice: 72000,
    currentPrice: 74800,
  },
  {
    stockCode: "000660",
    stockName: "SK하이닉스",
    quantity: 3,
    avgPrice: 153000,
    currentPrice: 159300,
  },
];

function formatWon(value: number) {
  return `${value.toLocaleString()}원`;
}

export default function AssetPanel() {
  const availableCash = 7650000;
  const lockedCash = 350000;

  const totalStockValue = mockHoldings.reduce(
    (sum, stock) => sum + stock.currentPrice * stock.quantity,
    0
  );

  const totalAsset = availableCash + lockedCash + totalStockValue;

  return (
    <>
      <section className={styles.summaryGrid}>
        <article className={styles.summaryCard}>
          <span>총 자산</span>
          <strong>{formatWon(totalAsset)}</strong>
        </article>

        <article className={styles.summaryCard}>
          <span>가용 현금</span>
          <strong>{formatWon(availableCash)}</strong>
        </article>

        <article className={styles.summaryCard}>
          <span>주문 묶인 돈</span>
          <strong>{formatWon(lockedCash)}</strong>
        </article>

        <article className={styles.summaryCard}>
          <span>보유 주식 평가액</span>
          <strong>{formatWon(totalStockValue)}</strong>
        </article>
      </section>

      <section className={styles.card}>
        <div className={styles.cardHeader}>
          <div>
            <h2>보유 주식</h2>
            <p>현재 보유 중인 종목과 평가 금액입니다.</p>
          </div>
        </div>

        <div className={styles.tableWrap}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>종목명</th>
                <th>종목코드</th>
                <th>보유수량</th>
                <th>평균단가</th>
                <th>현재가</th>
                <th>평가금액</th>
              </tr>
            </thead>
            <tbody>
              {mockHoldings.map((stock) => (
                <tr key={stock.stockCode}>
                  <td>{stock.stockName}</td>
                  <td>{stock.stockCode}</td>
                  <td>{stock.quantity.toLocaleString()}주</td>
                  <td>{formatWon(stock.avgPrice)}</td>
                  <td>{formatWon(stock.currentPrice)}</td>
                  <td>{formatWon(stock.currentPrice * stock.quantity)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}