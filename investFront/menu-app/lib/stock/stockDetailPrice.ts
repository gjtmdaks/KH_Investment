import type { PriceResponse } from "@/lib/stock/stockDetailTypes";

export function mergePriceResponse(
  incoming: PriceResponse,
  prev: PriceResponse | null
): PriceResponse {
  return {
    ...incoming,
    changePrice: incoming.changePrice ?? prev?.changePrice ?? null,
    openPrice: incoming.openPrice ?? prev?.openPrice ?? null,
    highPrice: incoming.highPrice ?? prev?.highPrice ?? null,
    lowPrice: incoming.lowPrice ?? prev?.lowPrice ?? null,
    executionStrength:
      incoming.executionStrength ?? prev?.executionStrength ?? null,
    wsSubscribed: incoming.wsSubscribed ?? prev?.wsSubscribed,
    priceSource: incoming.priceSource ?? prev?.priceSource,
    quoteSession: incoming.quoteSession ?? prev?.quoteSession ?? null,
    marketDivCode: incoming.marketDivCode ?? prev?.marketDivCode ?? null,
    asOf: incoming.asOf ?? prev?.asOf ?? null,
    stale: incoming.stale ?? prev?.stale,
  };
}
