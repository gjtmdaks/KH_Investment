export type StockItem = {
  stockCode: string;
  stockName: string;
  marketType: string;
  sector: string;
  currentPrice: number;
  changeRate: number;
  volume: number;
  extraValue?: number;
};