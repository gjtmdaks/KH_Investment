import styles from "./stock.module.css";
import StockRow from "./StockRow";

export default function StockList({ stocks }: any) {
  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <div></div>
        <div></div>
        <div>종목명</div>
        <div>현재가</div>
        <div>등락률</div>
        <div>거래량</div>
        <div>거래대금</div>
        <div></div>
      </div>

      {stocks.map((stock: any, index: number) => (
        <StockRow
          key={stock.stockCode}
          stock={stock}
          rank={index + 1}
        />
      ))}
    </div>
  );
}