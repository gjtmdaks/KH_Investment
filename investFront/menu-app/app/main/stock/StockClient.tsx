"use client";

import { useEffect, useState } from "react";
import StockList from "./StockList";

const rawBase = process.env.NEXT_PUBLIC_API_URL?.replace(/\/$/, "") ?? "";
const apiBase = rawBase.trim() || "http://localhost:8081/final";

export default function StockClient({ initialData }: any) {
  const [data, setData] = useState(initialData);

  useEffect(() => {
    const interval = setInterval(() => {
      fetch(`${apiBase}/api/main`, { credentials: "include" })
        .then((res) => {
          if (!res.ok) throw new Error(`HTTP ${res.status}`);
          return res.json();
        })
        .then(setData)
        .catch(() => {});
    }, 3000); // 3초

    return () => clearInterval(interval);
  }, []);

  if (!data) return <div>로딩중...</div>;

  return (
    <div>
        {data?.main?.stockList?.length === 0 ?
        <div>데이터 없음</div> : 
        <StockList stocks={data?.main?.stockList || []}/>
        }
    </div>
  );
}