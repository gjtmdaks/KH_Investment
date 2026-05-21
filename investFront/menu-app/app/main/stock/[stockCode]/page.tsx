import StockDetailClient from "./StockDetailClient";
import StockDetailUnavailablePage from "./StockDetailUnavailablePage";
import { resolveStockRegistration } from "@/lib/stock/resolveStockRegistration";

type StockDetailPageProps = {
  params: Promise<{
    stockCode: string;
  }>;
};

export default async function StockDetailPage({ params }: StockDetailPageProps) {
  const { stockCode: rawStockCode } = await params;
  const { stockCode, registered } =
    await resolveStockRegistration(rawStockCode);

  if (!registered) {
    return <StockDetailUnavailablePage />;
  }

  return <StockDetailClient stockCode={stockCode} />;
}
