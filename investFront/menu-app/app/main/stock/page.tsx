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
    <div style={{ padding: "30px" }}>
      <h1
        style={{
          fontSize: "32px",
          fontWeight: 700,
          marginBottom: "24px",
        }}
      >
        주식
      </h1>
      <StockClient initialData={data} />
    </div>
  );
}