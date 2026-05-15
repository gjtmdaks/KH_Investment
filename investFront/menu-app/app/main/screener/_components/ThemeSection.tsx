import StockCard from "./StockCard";
import { StockItem } from "./types";
import styles from "../ScreenerPage.module.css";

export default function ThemeSection({
  title,
  stocks,
}: {
  title: string;
  stocks: StockItem[];
}) {
  return (
    <section className={styles.themeSection}>
      <div className={styles.sectionHeader}>
        <h2>{title}</h2>
      </div>

      <div className={styles.stockList}>
        {stocks.map((stock) => (
          <StockCard
            key={stock.stockCode}
            stock={stock}
          />
        ))}
      </div>
    </section>
  );
}