"use client";

import styles from "../MainSidebar.module.css";
import SidebarStockItem from "../components/SidebarStockItem";
import SidebarSectionTitle from "../components/SidebarSectionTitle";
import useSidebarWatchlist from "../hooks/useSidebarWatchlist";

export default function InterestPanel() {
  const {
    loading,
    sidebarData,
    watchlist,
    setWatchlist,
  } = useSidebarWatchlist();

  if (loading) {
    return (
      <div className={styles.panelContent}>
        로딩중...
      </div>
    );
  }

  const {
    loggedIn,
    hasWatchlist,
    stockList,
  } = sidebarData;

  return (
    <div className={styles.panelContent}>
      <SidebarSectionTitle
        title={
          hasWatchlist
            ? "관심 종목"
            : "실시간 현재가 TOP 10"
        }
        description={
          hasWatchlist
            ? "등록한 관심종목"
            : "관심종목이 없어요"
        }
      />

      {stockList.length === 0 ? (
        <div>
          표시할 종목이 없습니다.
        </div>
      ) : (
        stockList.map(stock => (
          <SidebarStockItem
            key={stock.stockCode}
            stock={stock}
            watchlist={watchlist}
            setWatchlist={setWatchlist}
          />
        ))
      )}

      {!loggedIn && (
        <div className={styles.newsBox}>
          <p className={styles.newsCategory}>
            로그인 안내
          </p>

          <p className={styles.newsTitle}>
            로그인하면 관심종목을
            저장할 수 있어요.
          </p>
        </div>
      )}
    </div>
  );
}