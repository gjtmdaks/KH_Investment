"use client";

import { useState } from "react";
import styles from "./MainSidebar.module.css";

type SidebarMenu = "myInvestment" | "interest" | "recent" | "liveTime";

const menus: {
  id: SidebarMenu;
  label: string;
  icon: string;
}[] = [
  { id: "myInvestment", label: "내 투자", icon: "📈" },
  { id: "interest", label: "관심", icon: "♥" },
  { id: "recent", label: "최근 본", icon: "🕘" },
  { id: "liveTime", label: "실시간", icon: "🔥" },
];

export default function MainSidebar() {
  const [activeMenu, setActiveMenu] = useState<SidebarMenu>("interest");
  const [isOpen, setIsOpen] = useState(true);

  return (
    <aside className={`${styles.sidebar} ${isOpen ? styles.open : styles.close}`}>
      {isOpen && (
        <section className={styles.panel}>
          <div className={styles.panelHeader}>
            <h2>{getPanelTitle(activeMenu)}</h2>

            <button
              type="button"
              className={styles.closeButton}
              onClick={() => setIsOpen(false)}
            >
              〉
            </button>
          </div>

          {activeMenu === "myInvestment" && <MyInvestmentPanel />}
          {activeMenu === "interest" && <InterestPanel />}
          {activeMenu === "recent" && <RecentPanel />}
          {activeMenu === "liveTime" && <LiveTimePanel />}
        </section>
      )}

      <nav className={styles.iconNav}>
        {!isOpen && (
          <button
            type="button"
            className={styles.openButton}
            onClick={() => setIsOpen(true)}
          >
            〈
          </button>
        )}

        {menus.map((menu) => (
          <button
            key={menu.id}
            type="button"
            onClick={() => {
              setActiveMenu(menu.id);
              setIsOpen(true);
            }}
            className={`${styles.navButton} ${
              activeMenu === menu.id ? styles.active : ""
            }`}
          >
            <span className={styles.icon}>{menu.icon}</span>
            <span className={styles.label}>{menu.label}</span>
          </button>
        ))}
      </nav>
    </aside>
  );
}

function getPanelTitle(activeMenu: SidebarMenu) {
  switch (activeMenu) {
    case "myInvestment":
      return "내 투자";
    case "interest":
      return "관심";
    case "recent":
      return "최근 본";
    case "liveTime":
      return "실시간";
  }
}

function InterestPanel() {
  return (
    <div className={styles.panelContent}>
      <div className={styles.newsBox}>
        <p className={styles.newsCategory}>토스증권 AI</p>
        <p className={styles.newsTitle}>
          엔비디아 경쟁 심화와 투자심리 위축으로...
        </p>
      </div>

      <div className={styles.sectionTitle}>
        <h3>관심 주식 TOP 10</h3>
        <p>관심 그룹에 담아보세요</p>
      </div>

      <StockItem name="대한전선" price="68,900원" rate="+8,200원 (13.50%)" />
      <StockItem name="삼성전자" price="260,000원" rate="+27,500원 (11.82%)" />
      <StockItem name="SK하이닉스" price="1,593,000원" rate="+146,000원 (10.08%)" />
      <StockItem name="두산퓨얼셀" price="77,800원" rate="+17,200원 (28.38%)" />

      <button className={styles.addButton}>＋ 추가하기</button>
    </div>
  );
}

function MyInvestmentPanel() {
  return (
    <div className={styles.emptyBox}>
      <div className={styles.emptyIcon}>📊</div>
      <p>로그인이 필요해요</p>
    </div>
  );
}

function RecentPanel() {
  return (
    <div className={styles.panelContent}>
      <StockItem name="삼성전자" price="260,000원" rate="+27,500원 (11.82%)" />
      <StockItem name="카카오" price="58,200원" rate="-500원 (-0.85%)" />
    </div>
  );
}

function LiveTimePanel() {
  return (
    <div className={styles.panelContent}>
      <StockItem name="삼성전자" price="260,000원" rate="+27,500원 (11.82%)" />
      <StockItem name="현대차" price="251,000원" rate="-2,500원 (-0.98%)" />
    </div>
  );
}

function StockItem({
  name,
  price,
  rate,
}: {
  name: string;
  price: string;
  rate: string;
}) {
  const isUp = rate.startsWith("+");

  return (
    <div className={styles.stockItem}>
      <div className={styles.stockLogo}>{name.slice(0, 1)}</div>

      <div className={styles.stockName}>{name}</div>

      <div className={styles.stockPrice}>
        <strong>{price}</strong>
        <span className={isUp ? styles.up : styles.down}>{rate}</span>
      </div>

      <button className={styles.heartButton}>♥</button>
    </div>
  );
}