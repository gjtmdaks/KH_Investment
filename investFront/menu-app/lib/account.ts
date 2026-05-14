import { apiClient } from "@/lib/api-client";

export type AccountSummary = {
  currentTotalAsset: number;

  previousTotalAsset: number;
  dailyProfitAmount: number;
  dailyProfitRate: number;

  availableCash: number;
  stockValue: number;

  baseCapital: number;
  baseProfitAmount: number;
  baseProfitRate: number;

  accountStatus: string;
  createdAt: string;
};

export type HoldingStock = {
  stockCode: string;
  stockName: string;
  quantity: number;
  avgPrice: number;
  currentPrice: number;
  stockValue: number;
};

export type AccountAssetResponse = {
  totalAsset: number;
  availableCash: number;
  lockedCash: number;
  totalStockValue: number;
  holdings: HoldingStock[];
};

export async function getAccountSummary(userNo: number) {
  const response = await apiClient.get<AccountSummary>(
    `/account/summary?userNo=${userNo}`
  );

  return response.data;
}

export async function getAccountAssets() {
  const response = await apiClient.get<AccountAssetResponse>("/account/assets");
  return response.data;
}