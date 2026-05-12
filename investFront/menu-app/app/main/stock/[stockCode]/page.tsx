import StockDetailClient from "./StockDetailClient";

type StockDetailPageProps = {
  params: Promise<{
    stockCode: string;
  }>;
};

export default async function StockDetailPage({ params }: StockDetailPageProps) {
  const { stockCode } = await params;

  return (
    <StockDetailClient stockCode={stockCode} />
  );
}