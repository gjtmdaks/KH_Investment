"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/app/context/AuthContext";
import styles from "./MainSidebar.module.css";
import { SidebarMenu } from "./types";
import InterestPanel from "./panels/InterestPanel";
import RecentPanel from "./panels/RecentPanel";
import LiveTimePanel from "./panels/LiveTimePanel";
import AdminPanel from "./panels/AdminPanel";
import MyInvestmentPanel from "./panels/MyInvestmentPanel";

const baseMenus = [
  {
    id: "myInvestment",
    label: "내 투자",
    icon: "📈",
  },
  {
    id: "interest",
    label: "관심",
    icon: "♥",
  },
  {
    id: "recent",
    label: "최근 본",
    icon: "🕘",
  },
  {
    id: "liveTime",
    label: "실시간",
    icon: "🔥",
  },
];

const adminMenu = {
  id: "admin",
  label: "관리자",
  icon: "⚙",
};

export default function MainSidebar({
  data,
}: any) {

  const [activeMenu, setActiveMenu] = useState<SidebarMenu>("interest");
  const [isOpen, setIsOpen] = useState(true);
  const { user, isAuthenticated } = useAuth();
  const isAdmin = isAuthenticated && user?.auth === 1;
  const visibleMenus = isAdmin ? [...baseMenus, adminMenu] : baseMenus;

  useEffect(() => {
    function handleResize() {
      setIsOpen(
        window.innerWidth >= 1400
      );
    }

    handleResize();

    window.addEventListener(
      "resize",
      handleResize
    );

    return () => {
      window.removeEventListener(
        "resize",
        handleResize
      );
    };
  }, []);

  return (
    <aside
      className={`${styles.sidebar}
      ${isOpen
        ? styles.open
        : styles.close}`}
    >

      {isOpen && (
        <section className={styles.panel}>
          <div className={styles.panelHeader}>
            <h2>
              {getPanelTitle(activeMenu)}
            </h2>

            <button
              type="button"
              className={styles.closeButton}
              onClick={() =>
                setIsOpen(false)
              }
            >
              〉
            </button>
          </div>

          {activeMenu === "myInvestment" && (
            <MyInvestmentPanel data={data} />
          )}

          {activeMenu === "interest" && (
            <InterestPanel />
          )}

          {activeMenu === "recent" && (
            <RecentPanel />
          )}

          {activeMenu === "liveTime" && (
            <LiveTimePanel />
          )}

          {activeMenu === "admin"
            && isAdmin && (
            <AdminPanel />
          )}
        </section>
      )}

      <nav className={styles.iconNav}>

        {visibleMenus.map((menu: any) => (
          <button
            key={menu.id}
            type="button"
            onClick={() => {
              setActiveMenu(menu.id);
              setIsOpen(true);
            }}
            className={`${styles.navButton}
            ${activeMenu === menu.id
              ? styles.active
              : ""}`}
          >

            <span className={styles.icon}>
              {menu.icon}
            </span>

            <span className={styles.label}>
              {menu.label}
            </span>
          </button>
        ))}
      </nav>
    </aside>
  );
}

function getPanelTitle(
  activeMenu: SidebarMenu
) {
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