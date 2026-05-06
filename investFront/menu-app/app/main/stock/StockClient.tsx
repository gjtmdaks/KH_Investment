"use client";

import { useEffect, useState } from "react";

export default function StockClient({ initialData }: any) {
  const [data, setData] = useState(initialData);

  useEffect(() => {
    const interval = setInterval(() => {
      fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/main`)
        .then(res => res.json())
        .then(setData);
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