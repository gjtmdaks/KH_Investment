import Link from "next/link";
import StockClient from "./StockClient";

async function getInitialData() {
  const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/main`, {
    cache: "no-store",
  });
  const json = await res.json();

  return json;
}

export default async function StockPage() {
  const data = await getInitialData();

  return (
    <div>
      <h2>주식</h2>
      <p>전체 종목 목록이 들어갈 영역입니다.</p>
      <StockClient initialData={data} />

      <Link href="/main/stock/005930">
        디테일(삼전)
      </Link>
    </div>
  );
}