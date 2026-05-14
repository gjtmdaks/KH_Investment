import type { OrderbookLevel, OrderbookResponse } from "@/lib/stock/stockDetailTypes";

export function normalizeOrderbookResponse(data: unknown): OrderbookResponse | null {
  if (!data || typeof data !== "object") {
    return null;
  }

  const raw = data as Partial<OrderbookResponse>;

  if (!raw.stockCode) {
    return null;
  }

  return {
    stockCode: raw.stockCode,
    asks: normalizeOrderbookLevels(raw.asks),
    bids: normalizeOrderbookLevels(raw.bids),
    totalAskQuantity: raw.totalAskQuantity ?? null,
    totalBidQuantity: raw.totalBidQuantity ?? null,
    expectedPrice: raw.expectedPrice ?? null,
    expectedQuantity: raw.expectedQuantity ?? null,
  };
}

export function normalizeOrderbookLevels(value: unknown): OrderbookLevel[] {
  if (!Array.isArray(value)) {
    return [];
  }

  return value
    .filter((item): item is OrderbookLevel => {
      return Boolean(item) && typeof item === "object" && "level" in item;
    })
    .map((item) => ({
      level: Number(item.level),
      price: item.price ?? null,
      quantity: item.quantity ?? null,
      quantityChange: item.quantityChange ?? null,
    }))
    .filter((item) => Number.isFinite(item.level));
}
