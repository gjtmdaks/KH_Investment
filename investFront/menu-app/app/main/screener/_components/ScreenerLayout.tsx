"use client";

import { useState } from "react";
import ScreenerSidebar from "./ScreenerSidebar";
import ThemeSection from "./ThemeSection";
import RealtimeSection from "./RealtimeSection";
import ScreenerClient from "./ScreenerClient";
import styles from "../ScreenerPage.module.css";

type MenuType =
  | "rising"
  | "falling"
  | "watchlist"
  | "viewed"
  | "volume"
  | "realtime"
  | "search";

export default function ScreenerLayout({
  data,
}: any) {
  const [menu, setMenu] =
    useState<MenuType>("rising");

  function renderContent() {
    switch (menu) {
      case "rising":
        return (
          <ThemeSection
            title="🔥 오늘 많이 오른 종목"
            stocks={data.rising}
          />
        );

      case "falling":
        return (
          <ThemeSection
            title="📉 오늘 많이 내린 종목"
            stocks={data.falling}
          />
        );

      case "watchlist":
        return (
          <ThemeSection
            title="⭐ 관심종목 인기"
            stocks={data.watchlist}
          />
        );

      case "viewed":
        return (
          <ThemeSection
            title="👀 사람들이 많이 보는 종목"
            stocks={data.viewed}
          />
        );

      case "volume":
        return (
          <ThemeSection
            title="💥 거래량 급증"
            stocks={data.volume}
          />
        );

      case "realtime":
        return <RealtimeSection />;

      case "search":
        return <ScreenerClient />;

      default:
        return null;
    }
  }

  return (
    <div className={styles.layout}>
      <ScreenerSidebar
        menu={menu}
        setMenu={setMenu}
      />

      <div className={styles.contentArea}>
        {renderContent()}
      </div>
    </div>
  );
}