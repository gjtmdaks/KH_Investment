const STOCK_CODE_PATTERN = /^[0-9]{6}$/;

export function normalizeStockCodeParam(raw: string): string {
  try {
    return decodeURIComponent(raw).trim();
  } catch {
    return raw.trim();
  }
}

export function isValidStockCodeFormat(stockCode: string): boolean {
  return STOCK_CODE_PATTERN.test(stockCode);
}
