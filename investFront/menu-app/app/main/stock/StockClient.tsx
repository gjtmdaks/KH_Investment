"use client";

import { useEffect, useState } from "react";

const rawBase = process.env.NEXT_PUBLIC_API_URL?.replace(/\/$/, "") ?? "";
const apiBase = rawBase.trim() || "http://localhost:8081";

export default function StockClient({ initialData }: any) {
  const [data, setData] = useState(initialData);

  useEffect(() => {
    const interval = setInterval(() => {
      const url = `${apiBase}/api/main`;
      fetch(url, { credentials: "include" })
        .then((res) => {
          if (!res.ok) throw new Error(`HTTP ${res.status}`);
          return res.json();
        })
        .then(setData)
        .catch(() => {});
    }, 1000); // 1초

    return () => clearInterval(interval);
  }, []);

  if (!data) return <div>로딩중...</div>;

  return (
    <div>
        {data?.main?.stockList?.length === 0 ? (
        <div>데이터 없음</div>
        ) : (
        data?.main?.stockList?.map((s: any) => (
            <div key={s.stockCode}>
            {s.stockName} - {s.price}
            </div>
        ))
        )}
    </div>
  );
}