export const INVESTOR_TREND_DATA_GUIDE = {
  title: "매매동향 데이터 안내",
  summary:
    "당일 거래 데이터는 장 마감 후 확정 데이터에 포함됩니다. 정규장 시간(9:00~15:30)에는 전일 수치로 표시될 수 있습니다.",
} as const;

export function getKstTodayTradeDateYmd(): string {
  return new Intl.DateTimeFormat("en-CA", {
    timeZone: "Asia/Seoul",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  })
    .format(new Date())
    .replaceAll("-", "");
}

export function isInvestorTradeDateToday(tradeDate?: string | null): boolean {
  if (!tradeDate || tradeDate.length < 8) {
    return false;
  }

  const normalized = tradeDate.replaceAll(/\D/g, "").slice(0, 8);
  return normalized === getKstTodayTradeDateYmd();
}
