"use client";

import { useEffect, useState } from "react";

import styles from "../ScreenerPage.module.css";

import RealtimeCard from "./RealtimeCard";
import { getPublicApiBase } from "@/lib/api-base";
import { StockItem } from "./types";

type RealtimeData = {
  surging: StockItem[];
  falling: StockItem[];
  active: StockItem[];
};

const EMPTY_REALTIME_DATA: RealtimeData = {
  surging: [],
  falling: [],
  active: [],
};

export default function RealtimeSection() {
  const [data, setData] =
    useState<RealtimeData | null>(null);

  useEffect(() => {
    async function fetchRealtime() {
      try {
        const res = await fetch(
          `${getPublicApiBase()}/stock/screener/realtime`
        );

        const contentType = res.headers.get("content-type") ?? "";

        if (!res.ok || !contentType.includes("application/json")) {
          setData(EMPTY_REALTIME_DATA);
          return;
        }

        const json = await res.json();

        setData({
          surging: Array.isArray(json?.surging) ? json.surging : [],
          falling: Array.isArray(json?.falling) ? json.falling : [],
          active: Array.isArray(json?.active) ? json.active : [],
        });
      } catch {
        setData(EMPTY_REALTIME_DATA);
      }
    }

    fetchRealtime();
  }, []);

  if (!data) {
    return (
      <section className={styles.realtimeSection}>
        로딩중...
      </section>
    );
  }

  return (
    <section className={styles.realtimeSection}>
      <RealtimeCard
        title="실시간 급등"
        stocks={data.surging}
      />

      <RealtimeCard
        title="실시간 급락"
        stocks={data.falling}
      />

      <RealtimeCard
        title="실시간 체결 활발"
        stocks={data.active}
      />
    </section>
  );
}