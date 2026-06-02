import StockClient from "./StockClient";

export default function StockPage() {
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
      <StockClient />
    </div>
  );
}