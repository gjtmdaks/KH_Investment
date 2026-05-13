import { apiClient } from "@/lib/api-client";

export type AccountSummary = {
  currentTotalAsset: number;
  profitAmount: number;
  profitRate: number;
  availableCash: number;
  stockValue: number;
  initialBalance: number;
  initialProfitRate: number;
  accountStatus: string;
  createdAt: string;
};

export async function getAccountSummary(userNo: number) {
  const response = await apiClient.get<AccountSummary>(
    `/account/summary?userNo=${userNo}`
  );

  return response.data;
}