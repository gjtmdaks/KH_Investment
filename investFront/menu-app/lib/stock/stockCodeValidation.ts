/** KRX 6자리(숫자·영문 혼합, 예: 00104K) — DB STOCK_CODE VARCHAR2(8) 상한과 별도로 URL 파라미터는 6자리 기준 */
const STOCK_CODE_PATTERN = /^[0-9A-Za-z]{6}$/;

export function normalizeStockCodeParam(raw: string): string {
  let decoded: string;
  try {
    decoded = decodeURIComponent(raw).trim();
  } catch {
    decoded = raw.trim();
  }
  return decoded.toUpperCase();
}

export function isValidStockCodeFormat(stockCode: string): boolean {
  return STOCK_CODE_PATTERN.test(stockCode);
}
