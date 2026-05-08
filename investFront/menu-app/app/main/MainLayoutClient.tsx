"use client";

import { useEffect, useState } from "react";
import Header from "../components/header/Header";
import MainSidebar from "../components/sidebar/MainSidebar";
import styles from "./MainLayout.module.css";

const apiBase =
  process.env.NEXT_PUBLIC_API_URL?.replace(/\/$/, "").trim() ||
  "http://localhost:8081";

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

export default function MainLayoutClient({
  children,
}: {
  children: React.ReactNode;
}) {
  const [data, setData] = useState<unknown | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);

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

  if (loadError) {
    return (
      <div className={styles.pageLayout}>
        <div className={styles.leftArea}>
          <p className={styles.loadError}>{loadError}</p>
          <div className={styles.scrollRegion}>
            <div className={styles.bodyRow}>
              <main className={styles.content}>{children}</main>
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
          <div className={styles.scrollRegion}>
            <div className={styles.bodyRow}>
              <main className={styles.content}>{children}</main>
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

        <div className={styles.scrollRegion}>
          <div className={styles.bodyRow}>
            <main className={styles.content}>{children}</main>
          </div>
        </div>
      </div>
      <MainSidebar data={data} />
    </div>
  );
}
