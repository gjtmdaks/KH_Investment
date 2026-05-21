import type { ChartPeriodLabel, TabKey } from "@/lib/stock/stockDetailTypes";

export const HERO_QUOTE_REFRESH_INTERVAL_MS = 2_500;
/** WS 구독 풀 종목: 서버 DB/Redis/로컬 캐시 경로 — KIS REST 없이 빠른 Hero 갱신 */
export const HERO_QUOTE_WS_SUBSCRIBED_INTERVAL_MS = 800;
export const ORDERBOOK_REFRESH_INTERVAL_MS = 2_500;
/** 호가 H0STASP0 on-demand 구독 중: 서버 메모리 캐시 경로 — KIS REST 없이 빠른 호가 갱신 */
export const ORDERBOOK_WS_SUBSCRIBED_INTERVAL_MS = 800;
export const STOCK_NEWS_PAGE_SIZE = 5;

export const STOCK_INVESTOR_TREND_DAYS = 30;

export const stockDetailTabs: Array<{ key: TabKey; label: string }> = [
  { key: "chart", label: "차트" },
  { key: "orderbook", label: "호가" },
  { key: "investor", label: "개인·외국인·기관" },
  { key: "summary", label: "종목정보" },
  { key: "news", label: "뉴스" },
  { key: "community", label: "커뮤니티" },
];

export const minuteChartPeriods: ChartPeriodLabel[] = [
  "1분",
  "15분",
  "30분",
  "60분",
];

export const barChartPeriods: ChartPeriodLabel[] = ["일", "주", "월", "년"];

export const chartPeriods: ChartPeriodLabel[] = [
  ...minuteChartPeriods,
  ...barChartPeriods,
];
