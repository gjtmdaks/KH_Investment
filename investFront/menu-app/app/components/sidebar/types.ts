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

export interface MyInvestmentAccount {
  availableCash: number;
  investedAmount: number;
}

export interface MyInvestmentHolding {
  stockCode: string;
  stockName: string;
  quantity: number;
  avgPrice: number;
  currentPrice: number;
  stockValue: number;
  profitAmount: number;
  profitRate: number;
  dailyChangeRate: number;
}

export interface MyInvestmentSidebarData {
  account: MyInvestmentAccount | null;
  holdings: MyInvestmentHolding[];
}