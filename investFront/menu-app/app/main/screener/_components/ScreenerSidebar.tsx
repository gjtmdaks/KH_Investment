"use client";

import styles from "../ScreenerPage.module.css";

const menus = [
  {
    key: "rising",
    label: "🔥 급상승 종목",
  },
  {
    key: "falling",
    label: "📉 급하락 종목",
  },
  {
    key: "watchlist",
    label: "⭐ 관심종목 인기",
  },
  {
    key: "viewed",
    label: "👀 많이 보는 종목",
  },
  {
    key: "volume",
    label: "💥 거래량 급증",
  },
  {
    key: "realtime",
    label: "⚡ 실시간 급등",
  },
  {
    key: "search",
    label: "🔍 직접 찾기",
  },
];

export default function ScreenerSidebar({
  menu,
  setMenu,
}: any) {
  return (
    <aside className={styles.sidebar}>
      <div className={styles.sidebarTitle}>
        주식 탐색
      </div>

      <div className={styles.menuList}>
        {menus.map((item) => (
          <button
            key={item.key}
            className={
              menu === item.key
                ? styles.activeMenu
                : styles.menuButton
            }
            onClick={() => setMenu(item.key)}
          >
            {item.label}
          </button>
        ))}
      </div>
    </aside>
  );
}