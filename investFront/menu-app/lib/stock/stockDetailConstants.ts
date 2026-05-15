import type { ChartPeriodLabel, TabKey } from "@/lib/stock/stockDetailTypes";

export const QUOTE_REFRESH_INTERVAL_MS = 30_000;
export const STOCK_NEWS_PAGE_SIZE = 5;

export const stockDetailTabs: Array<{ key: TabKey; label: string }> = [
  { key: "chart", label: "차트" },
  { key: "orderbook", label: "호가" },
  { key: "summary", label: "종목정보" },
  { key: "news", label: "뉴스" },
  { key: "community", label: "커뮤니티" },
];

export const chartPeriods: ChartPeriodLabel[] = [
  "1분",
  "15분",
  "30분",
  "60분",
  "일",
  "주",
  "월",
  "년",
];
