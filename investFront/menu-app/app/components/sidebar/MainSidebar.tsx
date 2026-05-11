"use client";

import { useEffect, useState } from "react";
import styles from "./MainSidebar.module.css";

const rawBase = process.env.NEXT_PUBLIC_API_URL?.replace(/\/$/, "") ?? "";
const apiBase = rawBase.trim() || "http://localhost:8081/final";

type SidebarMenu = "myInvestment" | "interest" | "recent" | "liveTime" | "admin";

const baseMenus: {
  id: SidebarMenu;
  label: string;
  icon: string;
}[] = [
  { id: "myInvestment", label: "내 투자", icon: "📈" },
  { id: "interest", label: "관심", icon: "♥" },
  { id: "recent", label: "최근 본", icon: "🕘" },
  { id: "liveTime", label: "실시간", icon: "🔥" },
];

const adminMenu: {
  id: SidebarMenu;
  label: string;
  icon: string;
} = { id: "admin", label: "관리자", icon: "⚙" };

export default function MainSidebar({ data }: any) {
  const [activeMenu, setActiveMenu] = useState<SidebarMenu>("interest");
  const [isOpen, setIsOpen] = useState(true);
  const [localUser, setLocalUser] = useState<any>(null);

  const isLogin = !!data?.header?.userName || !!localUser?.userName;

  const isAdmin = localUser?.auth === 1;
  const visibleMenus = isAdmin ? [...baseMenus, adminMenu] : baseMenus;

  useEffect(() => {
    function handleResize() {
      setIsOpen(window.innerWidth >= 1400);
    }

    handleResize();

    window.addEventListener("resize", handleResize);

    return () => {
      window.removeEventListener("resize", handleResize);
    };
  }, []);

  useEffect(() => {
    const savedUser = window.localStorage.getItem("user");

    if (!savedUser) {
      setLocalUser(null);
      return;
    }

    try {
      setLocalUser(JSON.parse(savedUser));
    } catch {
      setLocalUser(null);
    }
  }, []);
  
  useEffect(() => {
    if (activeMenu === "admin" && !isAdmin) {
      setActiveMenu("interest");
    }
  }, [activeMenu, isAdmin]);

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

          {activeMenu === "myInvestment" && (
            <MyInvestmentPanel data={data} isLogin={isLogin} />
          )}
          {activeMenu === "interest" && <InterestPanel data={data} />}
          {activeMenu === "recent" && <RecentPanel data={data} />}
          {activeMenu === "liveTime" && <LiveTimePanel />}
          {activeMenu === "admin" && isAdmin && <AdminPanel />}
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

        {visibleMenus.map((menu) => (
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
    case "admin":
      return "관리자";
  }
}

function InterestPanel({ data }: any) {
  const list = data?.sidebar?.watchlist || [];

  return (
    <div className={styles.panelContent}>
      <h3>관심 종목</h3>

      {list.length === 0 ? (
        <div>데이터 없음</div>
        ) : (
          list.map((s: any) => (
          <StockItem
            key={s.stockCode}
            name={s.stockName}
            price={`${s.price}`}
            rate={`${s.changeRate}%`}
          />
        ))
      )}

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

function MyInvestmentPanel({ data, isLogin }: any) {
  if (!isLogin) {
    return (
      <div className={styles.emptyBox}>
        <div className={styles.emptyIcon}>📊</div>
        <p>로그인이 필요해요</p>
      </div>
    );
  }

  const account = data.sidebar?.account;
  const holdings = data.sidebar?.holdings || [];

  return (
    <div className={styles.panelContent}>
      <div>잔액: {account?.balance ?? 0}</div>

      <h4>보유 주식</h4>

      {holdings.length === 0 ? (
        <div>보유 주식 없음</div>
      ) : (
        holdings.map((h: any) => (
          <StockItem
            key={h.stockCode}
            name={h.stockName}
            price={`${h.currentPrice}`}
            rate={`${h.quantity}주`}
          />
        ))
      )}
    </div>
  );
}

function RecentPanel({ data }: any) {
  const list = data?.sidebar?.recentView || [];

  return (
    <div className={styles.panelContent}>
      {list.length === 0 ? (
        <div>최근 본 종목 없음</div>
      ) : (
        list.map((s: any) => (
          <StockItem
            key={s.stockCode}
            name={s.stockName}
            price="-"
            rate="-"
          />
        ))
      )}

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

function AdminPanel() {

  const [companyLoading, setCompanyLoading] = useState(false);
  const [historyLoading, setHistoryLoading] = useState(false);

  async function requestCompanySync() {

    try {

      setCompanyLoading(true);

      const response = await fetch(
        `${apiBase}/admin/api/init`,
        {
          method: "POST",
        }
      );

      if (!response.ok) {
        throw new Error("회사 정보 동기화 실패");
      }

      alert("회사 정보 동기화 시작");

    } catch (e) {

      console.error(e);

      alert("회사 정보 동기화 실패");
    } finally {
      setCompanyLoading(false);
    }
  }

  async function requestHistorySync() {

    try {

      setHistoryLoading(true);

      const response = await fetch(
        `${apiBase}/admin/api/kis/historysync`,
        {
          method: "POST",
        }
      );

      if (!response.ok) {
        throw new Error("과거 시세 동기화 실패");
      }

      alert("과거 시세 동기화 시작");

    } catch (e) {

      console.error(e);

      alert("과거 시세 동기화 실패");

    } finally {
      setHistoryLoading(false);
    }
  }

  return (
    <div className={styles.panelContent}>

      <div className={styles.sectionTitle}>
        <h3>관리자 기능</h3>
        <p>초기 적재 및 데이터 동기화</p>
      </div>

      <button
        className={styles.addButton}
        onClick={requestCompanySync}
        disabled={companyLoading}
      >
        회사 정보 동기화
      </button>

      <button
        className={styles.addButton}
        onClick={requestHistorySync}
        disabled={historyLoading}
      >
        과거 시세 동기화
      </button>

    </div>
  );
}