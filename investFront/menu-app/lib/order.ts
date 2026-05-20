import axios from "axios";
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
export type OrderHistoryResponse = {
  orderId: number;
  orderKind: OrderKind;
  orderType: OrderType;
  stockCode: string;
  stockName: string;
  price: number;
  quantity: number;
  status: string;
  createdAt: string;
};
type ApiFailureResponse = {
  success: false;
  data: unknown;
  message?: string;
};

function isApiFailureResponse(data: unknown): data is ApiFailureResponse {
  return (
    data != null &&
    typeof data === "object" &&
    "success" in data &&
    (data as ApiFailureResponse).success === false
  );
}
export async function getOrderHistory() {
  const response = await apiClient.get<OrderHistoryResponse[]>("/orders/history");
  return response.data;
}
export async function createOrder(request: OrderRequest) {
  try {
    const response = await apiClient.post<OrderResponse | ApiFailureResponse>(
      "/orders",
      request
    );

    if (isApiFailureResponse(response.data)) {
      throw new Error(response.data.message || "주문 처리에 실패했습니다.");
    }

    return response.data;
  } catch (error) {
    throw new Error(getApiErrorMessage(error, "주문 처리에 실패했습니다."));
  }
}
export async function getTradeHistory() {
  const response = await apiClient.get<TradeResponse[]>("/orders/trades");
  return response.data;
}
export async function cancelOrder(orderId: number) {
  await apiClient.patch(`/orders/${orderId}/cancel`);
}

export async function updateOrderPrice(orderId: number, price: number) {
  await apiClient.patch(`/orders/${orderId}/price`, {
    price,
  });
}

function getApiErrorMessage(error: unknown, fallbackMessage: string) {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data;

    if (typeof data === "string") {
      return data;
    }

    if (data && typeof data === "object" && "message" in data) {
      const message = data.message;

      if (typeof message === "string" && message.trim()) {
        return message;
      }
    }
  }

  if (error instanceof Error && error.message) {
    return error.message;
  }

  return fallbackMessage;
}