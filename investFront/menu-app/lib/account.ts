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

export async function getAccountSummary(userNo: number) {
  const response = await apiClient.get<AccountSummary>(
    `/account/summary?userNo=${userNo}`
  );

  return response.data;
}