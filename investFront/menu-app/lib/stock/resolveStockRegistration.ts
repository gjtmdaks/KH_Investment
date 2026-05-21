import { getPublicApiBase } from "@/lib/api-base";
import {
  isValidStockCodeFormat,
  normalizeStockCodeParam,
} from "@/lib/stock/stockCodeValidation";

type StockInfoResponse = {
  stockCode?: string;
};

export async function resolveStockRegistration(rawStockCode: string): Promise<{
  stockCode: string;
  registered: boolean;
}> {
  const stockCode = normalizeStockCodeParam(rawStockCode);

  if (!isValidStockCodeFormat(stockCode)) {
    return { stockCode, registered: false };
  }

  const apiBase = getPublicApiBase();

  try {
    const response = await fetch(`${apiBase}/stock/${stockCode}/info`, {
      cache: "no-store",
    });

    if (!response.ok) {
      return { stockCode, registered: false };
    }

    const data = (await response.json()) as StockInfoResponse;

    return {
      stockCode,
      registered: Boolean(data.stockCode),
    };
  } catch {
    return { stockCode, registered: false };
  }
}
