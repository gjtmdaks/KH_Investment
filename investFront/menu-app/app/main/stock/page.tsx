import StockClient from "./StockClient";
import { getPublicApiBase } from "@/lib/api-base";

async function getInitialData() {
  const apiBase = getPublicApiBase();
  const res = await fetch(`${apiBase}/api/main`, {
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