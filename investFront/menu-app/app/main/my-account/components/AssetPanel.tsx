"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import styles from "../myAccount.module.css";
import {
  getAccountAssets,
  type AccountAssetResponse,
} from "@/lib/account";

function formatWon(value?: number | null) {
  return `${(value ?? 0).toLocaleString()}원`;
}

function formatSignedWon(value?: number | null) {
  const numberValue = value ?? 0;
  const sign = numberValue > 0 ? "+" : "";
  return `${sign}${numberValue.toLocaleString()}원`;
}

function formatRate(value?: number | null) {
  const numberValue = value ?? 0;
  const sign = numberValue > 0 ? "+" : "";
  return `${sign}${numberValue.toFixed(2)}%`;
}

function getProfitClass(value?: number | null) {
  const numberValue = value ?? 0;

  if (numberValue > 0) return styles.profitUp;
  if (numberValue < 0) return styles.profitDown;
  return styles.profitFlat;
}

export default function AssetPanel() {
  const router = useRouter();
  const [asset, setAsset] = useState<AccountAssetResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    async function fetchAccountAssets() {
      try {
        setLoading(true);
        setErrorMessage(null);

        const data = await getAccountAssets();

        if (!isMounted) return;

        setAsset({
          ...data,
          holdings: Array.isArray(data.holdings) ? data.holdings : [],
        });
      } catch (error) {
        console.error(error);

        if (!isMounted) return;

        setErrorMessage("자산 정보를 불러오지 못했습니다.");
        setAsset(null);
      } finally {
        if (!isMounted) return;
        setLoading(false);
      }
    }

    fetchAccountAssets();

    return () => {
      isMounted = false;
    };
  }, []);

  if (loading) {
    return (
      <section className={styles.card}>
        <div className={styles.cardHeader}>
          <div>
            <h2>자산</h2>
            <p>자산 정보를 불러오는 중입니다.</p>
          </div>
        </div>
      </section>
    );
  }

  if (errorMessage || !asset) {
    return (
      <section className={styles.card}>
        <div className={styles.cardHeader}>
          <div>
            <h2>자산</h2>
            <p>{errorMessage ?? "자산 정보를 찾을 수 없습니다."}</p>
          </div>
        </div>
      </section>
    );
  }
  const totalBuyAmount = asset.holdings.reduce(
    (sum, stock) => sum + stock.avgPrice * stock.quantity,
    0
  );

  const totalProfitAmount = asset.totalStockValue - totalBuyAmount;

  const totalProfitRate =
    totalBuyAmount > 0 ? (totalProfitAmount / totalBuyAmount) * 100 : 0;
  return (
    <>
      <section className={styles.summaryGrid}>
        <article className={styles.summaryCard}>
          <span>총 자산</span>
          <strong>{formatWon(asset.totalAsset)}</strong>
        </article>

        <article className={styles.summaryCard}>
          <span>가용 현금</span>
          <strong>{formatWon(asset.availableCash)}</strong>
        </article>

        <article className={styles.summaryCard}>
          <span>주문 묶인 돈</span>
          <strong>{formatWon(asset.lockedCash)}</strong>
        </article>

        <article className={styles.summaryCard}>
          <span>보유 주식 평가액</span>
          <strong>{formatWon(asset.totalStockValue)}</strong>
        </article>

        <article className={`${styles.summaryCard} ${styles.wideSummaryCard}`}>
          <span>총 평가손익</span>
          <strong className={getProfitClass(totalProfitAmount)}>
            {formatSignedWon(totalProfitAmount)}
          </strong>
        </article>

        <article className={`${styles.summaryCard} ${styles.wideSummaryCard}`}>
          <span>총 수익률</span>
          <strong className={getProfitClass(totalProfitRate)}>
            {formatRate(totalProfitRate)}
          </strong>
        </article>
      </section>

      <section className={styles.card}>
        <div className={styles.cardHeader}>
          <div>
            <h2>보유 주식</h2>
            <p>현재 보유 중인 종목과 평가 금액입니다.</p>
          </div>
        </div>

        {asset.holdings.length === 0 ? (
          <div className={styles.infoList}>
            <div className={styles.infoRow}>
              <span>보유 주식</span>
              <strong>아직 보유 중인 주식이 없습니다.</strong>
            </div>
          </div>
        ) : (
          <div className={styles.tableWrap}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>종목코드</th>
                  <th>종목명</th>
                  <th>보유수량</th>
                  <th>평균단가</th>
                  <th>현재가</th>
                  <th>평가금액</th>
                  <th>평가손익</th>
                  <th>수익률</th>
                </tr>
              </thead>

              <tbody>
                {asset.holdings.map((stock) => {
                  const profitAmount =
                    (stock.currentPrice - stock.avgPrice) * stock.quantity;

                  const profitRate =
                    stock.avgPrice > 0
                      ? ((stock.currentPrice - stock.avgPrice) /
                          stock.avgPrice) *
                        100
                      : 0;

                  return (
                    <tr key={stock.stockCode}
                      className={styles.clickableRow}
                      onClick={() => router.push(`/main/stock/${stock.stockCode}`)} >
                      <td>{stock.stockCode}</td>
                      <td>{stock.stockName}</td>
                      <td>{stock.quantity.toLocaleString()}주</td>
                      <td>{formatWon(stock.avgPrice)}</td>
                      <td>{formatWon(stock.currentPrice)}</td>
                      <td>{formatWon(stock.stockValue)}</td>
                      <td className={getProfitClass(profitAmount)}>
                        {formatSignedWon(profitAmount)}
                      </td>
                      <td className={getProfitClass(profitRate)}>
                        {formatRate(profitRate)}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </>
  );
}