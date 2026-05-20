import type { PriceResponse } from "@/lib/stock/stockDetailTypes";
import { formatPercent, formatWon } from "@/lib/stock/stockDetailFormat";

export const DEFAULT_APP_DOCUMENT_TITLE = "khinvest";

export function buildStockDetailDocumentTitle(
  price: PriceResponse | null,
  displayName: string
): string | null {
  const name = displayName.trim();

  if (!name) {
    return null;
  }

  const current = formatWon(price?.currentPrice);

  if (current === "-") {
    return name;
  }

  const rate = formatPercent(price?.changeRate);
  const ratePart = rate === "-" ? "" : ` ${rate}`;

  return `${current}${ratePart} | ${name}`;
}
