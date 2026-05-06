import Link from "next/link";

export default function StockPage() {
  return (
    <div>
      <h2>주식</h2>
      <p>전체 종목 목록이 들어갈 영역입니다.</p>

      <Link href="/main/stock/005930">
        디테일(삼전)
      </Link>
    </div>
  );
}