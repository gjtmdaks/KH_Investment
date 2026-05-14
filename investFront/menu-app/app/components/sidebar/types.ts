export type SidebarMenu =
  | "myInvestment"
  | "interest"
  | "recent"
  | "liveTime"
  | "admin";

export interface SidebarStock {
  stockCode: string;
  stockName: string;
  currentPrice: number;
  changeRate: number;
  volume: number;
  tradingValue: number;
}

export interface SidebarWatchResponse {
  loggedIn: boolean;

  hasWatchlist: boolean;

  // 실제 관심종목 코드 목록
  watchlistCodes: string[];

  // 화면 표시 목록
  stockList: SidebarStock[];
}