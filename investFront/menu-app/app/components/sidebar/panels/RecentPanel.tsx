"use client";

import styles from "../MainSidebar.module.css";
import SidebarEmpty from "../components/SidebarEmpty";
import SidebarSectionTitle from "../components/SidebarSectionTitle";
import SidebarStockItem from "../components/SidebarStockItem";
import useRecentStocks from "../hooks/useRecentStocks";
import { useAuth } from "@/app/context/AuthContext";
import { useWatchlist } from "@/app/context/WatchlistContext";

const LOGIN_REQUIRED_TEXT = "로그인하면 이용할 수 있어요";

export default function RecentPanel() {
  const { isAuthenticated, isLoading: authLoading } = useAuth();
  const { loading, stocks } = useRecentStocks(isAuthenticated);
  const { watchlist, setWatchlist } = useWatchlist();

  if (authLoading) {
    return (
      <div className={styles.panelContent}>
        로딩중...
      </div>
    );
  }

  if (!isAuthenticated) {
    return <SidebarEmpty text={LOGIN_REQUIRED_TEXT} />;
  }

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
        stocks.map((stock) => (
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
