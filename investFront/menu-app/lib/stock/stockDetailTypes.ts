export type PriceResponse = {
  stockCode: string;
  stockName?: string | null;
  currentPrice?: string | null;
  changePrice?: string | null;
  changeRate?: string | null;
  volume?: string | null;
  tradingValue?: string | null;
  openPrice?: string | null;
  highPrice?: string | null;
  lowPrice?: string | null;
};

export type OrderbookLevel = {
  level: number;
  price?: string | null;
  quantity?: string | null;
  quantityChange?: string | null;
};

export type OrderbookResponse = {
  stockCode: string;
  asks: OrderbookLevel[];
  bids: OrderbookLevel[];
  totalAskQuantity?: string | null;
  totalBidQuantity?: string | null;
  expectedPrice?: string | null;
  expectedQuantity?: string | null;
};

export type StaticProfileResponse = {
  stockCode?: string | null;
  stockName?: string | null;
  marketType?: string | null;
  sector?: string | null;
  listedDate?: string | null;
  status?: string | null;
  corpCode?: string | null;
  coName?: string | null;
  issuedStock?: string | null;
  declinedStock?: string | null;
  treasuryStock?: string | null;
  outstandingShares?: string | null;
  shareholdingRatio?: string | null;
  ownershipPercentage?: string | null;
};

export type StockDetailResponse = {
  price: PriceResponse;
  profile: StaticProfileResponse | null;
};

export type NewsResponse = {
  newsInfoId: number;
  title: string;
  description?: string | null;
  publisher?: string | null;
  primaryLabel?: string | null;
  articleLink?: string | null;
  publishedAt?: string | null;
};

export type ChartPeriodLabel = "1분" | "15분" | "30분" | "60분" | "일" | "주" | "월" | "년";

export type TabKey = "chart" | "orderbook" | "summary" | "news";

export type NewsLoadPhase = "idle" | "loading" | "done";
