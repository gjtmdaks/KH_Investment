import styles from "../ScreenerPage.module.css";
import Link from "next/link";

export default function RealtimeCard({
  title,
  stocks,
}: any) {
  return (
    <div className={styles.realtimeCard}>
      <h3 className={styles.realtimeTitle}>
        {title}
      </h3>

      <div className={styles.realtimeList}>
        {stocks.map((stock: any) => {
          const isUp = stock.changeRate >= 0;

          return (
            <Link href={`/main/stock/${stock.stockCode}`}>
                <div
                key={stock.stockCode}
                className={styles.realtimeItem}
                >
                <div
                    className={
                    styles.realtimeStockName
                    }
                >
                    {stock.stockName}
                </div>

                <div
                    className={
                    isUp
                        ? styles.upRate
                        : styles.downRate
                    }
                >
                    {isUp ? "+" : ""}
                    {stock.changeRate}%
                </div>
                </div>
            </Link>
          );
        })}
      </div>
    </div>
  );
}