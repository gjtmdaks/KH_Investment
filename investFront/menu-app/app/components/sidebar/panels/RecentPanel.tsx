"use client";

import styles from "../MainSidebar.module.css";
import SidebarStockItem from "../components/SidebarStockItem";
import SidebarSectionTitle from "../components/SidebarSectionTitle";
import useRecentStocks from "../hooks/useRecentStocks";
import { useWatchlist } from "@/app/context/WatchlistContext";

export default function RecentPanel() {
  const {
    loading,
    stocks,
  } = useRecentStocks();

  const {
    watchlist,
    setWatchlist,
  } = useWatchlist();

  if (loading) {
    return (
      <div className={styles.panelContent}>
        로딩중...
      </div>
    );
  }

  return (
    <div className={styles.panelContent}>
      <SidebarSectionTitle
        title="최근 본 종목"
        description="최근 조회한 종목"
      />
      {stocks.length === 0 ? (
        <div>
          최근 본 종목이 없습니다.
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