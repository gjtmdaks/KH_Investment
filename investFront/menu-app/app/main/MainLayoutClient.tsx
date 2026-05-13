"use client";

import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";
import Header from "../components/header/Header";
import MainSidebar from "../components/sidebar/MainSidebar";
import styles from "./MainLayout.module.css";

import { API_BASE_URL } from "@/lib/api-base";
import { completeLogoutFromQuery } from "@/lib/auth-user";

const apiBase = API_BASE_URL;

async function fetchMainJson(): Promise<unknown> {
  const headers = new Headers({ Accept: "application/json" });
  const token = window.localStorage.getItem("accessToken");
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const res = await fetch(`${apiBase}/api/main`, {
    cache: "no-store",
    credentials: "include",
    headers,
  });
  if (!res.ok) {
    throw new Error(`HTTP ${res.status}`);
  }
  return res.json();
}

const STOCK_DETAIL_PATH = /^\/main\/stock\/[^/]+$/;

export default function MainLayoutClient({
  children,
}: {
  children: React.ReactNode;
}) {
  const pathname = usePathname();
  const isStockDetailViewport = STOCK_DETAIL_PATH.test(pathname ?? "");

  const [data, setData] = useState<unknown | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);

  useEffect(() => {
    completeLogoutFromQuery();
  }, []);

  useEffect(() => {
    let cancelled = false;
    setLoadError(null);
    fetchMainJson()
      .then((json) => {
        if (!cancelled) {
          const response = json as { success?: boolean; data?: unknown };
          setData(response.data ?? json);
        }
      })
      .catch(() => {
        if (!cancelled) {
          setLoadError("메인 정보를 불러오지 못했습니다.");
        }
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const scrollRegionClassName = isStockDetailViewport
    ? `${styles.scrollRegion} ${styles.scrollRegionStockDetail}`
    : styles.scrollRegion;
  const bodyRowClassName = isStockDetailViewport
    ? `${styles.bodyRow} ${styles.bodyRowStockDetail}`
    : styles.bodyRow;
  const contentClassName = isStockDetailViewport
    ? `${styles.content} ${styles.contentStockDetail}`
    : styles.content;

  if (loadError) {
    return (
      <div className={styles.pageLayout}>
        <div className={styles.leftArea}>
          <p className={styles.loadError}>{loadError}</p>
          <div className={scrollRegionClassName}>
            <div className={bodyRowClassName}>
              <main className={contentClassName}>{children}</main>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (data === null) {
    return (
      <div className={styles.pageLayout}>
        <div className={styles.leftArea}>
          <p className={styles.loadingMessage}>헤더·사이드 정보를 불러오는 중…</p>
          <div className={scrollRegionClassName}>
            <div className={bodyRowClassName}>
              <main className={contentClassName}>{children}</main>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.pageLayout}>
      <div className={styles.leftArea}>
        <Header data={data} />

        <div className={scrollRegionClassName}>
          <div className={bodyRowClassName}>
            <main className={contentClassName}>{children}</main>
          </div>
        </div>
      </div>
      <MainSidebar data={data} />
    </div>
  );
}
