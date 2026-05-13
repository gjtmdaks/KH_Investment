import { apiClient } from "@/lib/api-client";

export type OrderKind = "BUY" | "SELL";
export type OrderType = "LIMIT" | "MARKET";

export type OrderRequest = {
  stockCode: string;
  orderKind: OrderKind;
  orderType: OrderType;
  price: number;
  quantity: number;
};

export type OrderResponse = {
  stockCode: string;
  orderKind: OrderKind;
  orderType: OrderType;
  price: number;
  quantity: number;
  status: string;
  createdAt: string;
};

export async function createOrder(request: OrderRequest) {
  const response = await apiClient.post<OrderResponse>("/orders", request);
  return response.data;
}