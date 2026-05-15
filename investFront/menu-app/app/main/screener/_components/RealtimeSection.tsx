"use client";

import { useEffect, useState } from "react";

import styles from "../ScreenerPage.module.css";

import RealtimeCard from "./RealtimeCard";

export default function RealtimeSection() {
  const [data, setData] = useState<any>(null);

  useEffect(() => {
    async function fetchRealtime() {
      const res = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/stock/screener/realtime`
      );

      const json = await res.json();

      setData(json);
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