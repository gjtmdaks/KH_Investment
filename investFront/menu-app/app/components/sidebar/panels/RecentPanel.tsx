"use client";

import styles from "../MainSidebar.module.css";
import SidebarEmpty from "../components/SidebarEmpty";
import SidebarSectionTitle from "../components/SidebarSectionTitle";
import SidebarStockItem from "../components/SidebarStockItem";
import useRecentStocks from "../hooks/useRecentStocks";
import { useWatchlist } from "@/app/context/WatchlistContext";

interface Props {
  isLogin: boolean;
}

export default function RecentPanel({
  isLogin,
}: Props) {

  // 비로그인
  if (!isLogin) {
    return (
      <SidebarEmpty text="로그인이 필요해요" />
    );
  }

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
        <SidebarEmpty text="최근 본 종목이 없어요" />
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