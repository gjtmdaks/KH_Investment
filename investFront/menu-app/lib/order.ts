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

export type TradeResponse = {
  tradeId: number;
  orderKind: OrderKind;
  stockCode: string;
  stockName: string;
  price: number;
  quantity: number;
  executedAt: string;
};

export async function createOrder(request: OrderRequest) {
  const response = await apiClient.post<OrderResponse>("/orders", request);
  return response.data;
}
export async function getTradeHistory() {
  const response = await apiClient.get<TradeResponse[]>("/orders/trades");
  return response.data;
}