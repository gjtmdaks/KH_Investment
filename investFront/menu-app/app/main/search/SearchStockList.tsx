import Link from "next/link";
import styles from "./SearchStockList.module.css";
import type { StockItem } from "./page";

export default function SearchStockList({
  stocks,
}: {
  stocks: StockItem[];
}) {
  if (stocks.length === 0) {
    return (
      <div className={styles.empty}>
        검색된 종목이 없습니다.
      </div>
    );
  }

  return (
    <div className={styles.list}>
      {stocks.map((stock) => (
        <Link
          key={stock.stockCode}
          href={`/main/stock/${stock.stockCode}`}
          className={styles.item}
        >
          <div className={styles.left}>
            <div className={styles.stockName}>
              {stock.stockName}
            </div>

            <div className={styles.stockCode}>
              {stock.stockCode}
            </div>
          </div>

          <div className={styles.market}>
            {stock.marketType}
          </div>
        </Link>
      ))}
    </div>
  );
}