"use client";

import styles from "../MainSidebar.module.css";
import SidebarStockItem from "../components/SidebarStockItem";
import useRealtimeStocks from "../hooks/useRealtimeStocks";
import { useWatchlist } from "@/app/context/WatchlistContext";

export default function LiveTimePanel() {
  const {loading, stocks,} = useRealtimeStocks();
  const {watchlist, setWatchlist,} = useWatchlist();

  if (loading) {
    return (
      <div className={styles.panelContent}>
        로딩중...
      </div>
    );
  }

  return (
    <div className={styles.panelContent}>
      <div className={styles.sectionTitle}>
        <h3>
          실시간 상승목록
        </h3>
        <p>
          상승률 기준 TOP 종목
        </p>
      </div>

      {stocks.length === 0 ? (
        <div>
          데이터가 없습니다.
        </div>
      ) : (
        stocks.map(stock => (
          <SidebarStockItem
            key={stock.stockCode}
            stock={stock}
            watchlist={watchlist}
            setWatchlist={setWatchlist}
          />
        ))
      )}
    </div>
  );
}