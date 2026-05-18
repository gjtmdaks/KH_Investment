import { parseNumeric } from "@/lib/stock/stockDetailFormat";
import type { OrderbookLevel, PriceResponse } from "@/lib/stock/stockDetailTypes";

export type BestQuote = {
  price: number;
  level: OrderbookLevel;
};

export function getLevelPrice(level: OrderbookLevel): number | null {
  return parseNumeric(level.price);
}

export function getLevelQuantity(level: OrderbookLevel): number | null {
  return parseNumeric(level.quantity);
}

export function getBestAsk(asks: OrderbookLevel[]): BestQuote | null {
  let best: BestQuote | null = null;

  for (const level of asks) {
    const price = getLevelPrice(level);

    if (price === null || price <= 0) {
      continue;
    }

    if (!best || price < best.price) {
      best = { price, level };
    }
  }

  return best;
}

export function getBestBid(bids: OrderbookLevel[]): BestQuote | null {
  let best: BestQuote | null = null;

  for (const level of bids) {
    const price = getLevelPrice(level);

    if (price === null || price <= 0) {
      continue;
    }

    if (!best || price > best.price) {
      best = { price, level };
    }
  }

  return best;
}

export function getBestAskBid(asks: OrderbookLevel[], bids: OrderbookLevel[]) {
  return {
    bestAsk: getBestAsk(asks),
    bestBid: getBestBid(bids),
  };
}

export function calcSpread(bestAsk: number | null, bestBid: number | null): number | null {
  if (bestAsk === null || bestBid === null || bestAsk <= 0 || bestBid <= 0) {
    return null;
  }

  const spread = bestAsk - bestBid;

  return spread >= 0 ? spread : null;
}

export function calcMidPrice(bestAsk: number | null, bestBid: number | null): number | null {
  if (bestAsk === null || bestBid === null || bestAsk <= 0 || bestBid <= 0) {
    return null;
  }

  return (bestAsk + bestBid) / 2;
}

export function calcMaxQuantity(levels: OrderbookLevel[]): number {
  let max = 0;

  for (const level of levels) {
    const quantity = getLevelQuantity(level);

    if (quantity !== null && quantity > max) {
      max = quantity;
    }
  }

  return max;
}

export function calcDepthPercent(quantity: number | null, maxQuantity: number): number {
  if (quantity === null || quantity <= 0 || maxQuantity <= 0) {
    return 0;
  }

  return Math.min(100, Math.round((quantity / maxQuantity) * 100));
}

export function getReferencePrice(price: PriceResponse | null): number | null {
  const current = parseNumeric(price?.currentPrice);
  const change = parseNumeric(price?.changePrice);

  if (current === null) {
    return null;
  }

  if (change === null) {
    return current;
  }

  const reference = current - change;

  return reference > 0 ? reference : null;
}

export function calcPriceChangeRate(
  levelPrice: number | null,
  referencePrice: number | null
): number | null {
  if (levelPrice === null || referencePrice === null || referencePrice <= 0) {
    return null;
  }

  return ((levelPrice - referencePrice) / referencePrice) * 100;
}

export function isSamePrice(a: number | null, b: number | null): boolean {
  if (a === null || b === null) {
    return false;
  }

  return Math.abs(a - b) < 0.5;
}

export function isCurrentPriceRow(
  levelPrice: number | null,
  currentPrice: number | null
): boolean {
  return isSamePrice(levelPrice, currentPrice);
}

export function calcQuantityRatio(
  askTotal: number | null,
  bidTotal: number | null
): { askPercent: number; bidPercent: number } {
  const ask = askTotal ?? 0;
  const bid = bidTotal ?? 0;
  const sum = ask + bid;

  if (sum <= 0) {
    return { askPercent: 50, bidPercent: 50 };
  }

  const askPercent = Math.round((ask / sum) * 100);

  return {
    askPercent,
    bidPercent: 100 - askPercent,
  };
}
