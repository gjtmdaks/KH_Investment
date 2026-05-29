import { parseNumeric } from "@/lib/stock/stockDetailFormat";

export function calcShareRatio(
  part?: string | null,
  whole?: string | null
): number | null {
  const numerator = parseNumeric(part);
  const denominator = parseNumeric(whole);

  if (numerator === null || denominator === null || denominator === 0) {
    return null;
  }

  return (numerator / denominator) * 100;
}

export function parsePercentValue(value?: string | null): number | null {
  const numeric = parseNumeric(value);

  if (numeric === null) {
    return null;
  }

  return numeric;
}

export function formatListingDuration(listedDate?: string | null): string {
  if (!listedDate) {
    return "-";
  }

  const parsed = new Date(listedDate);

  if (Number.isNaN(parsed.getTime())) {
    return "-";
  }

  const now = new Date();
  let months =
    (now.getFullYear() - parsed.getFullYear()) * 12 +
    (now.getMonth() - parsed.getMonth());

  if (now.getDate() < parsed.getDate()) {
    months -= 1;
  }

  if (months < 0) {
    return "-";
  }

  const years = Math.floor(months / 12);
  const remainder = months % 12;

  if (years === 0) {
    return `${remainder}개월`;
  }

  if (remainder === 0) {
    return `${years}년`;
  }

  return `${years}년 ${remainder}개월`;
}

export function formatDayPriceRange(
  openPrice?: string | null,
  highPrice?: string | null,
  lowPrice?: string | null
): string {
  const open = parseNumeric(openPrice);
  const high = parseNumeric(highPrice);
  const low = parseNumeric(lowPrice);

  if (open === null || high === null || low === null || open === 0) {
    return "-";
  }

  const spread = high - low;
  const percent = (spread / open) * 100;

  return `${spread.toLocaleString("ko-KR")}원 (${percent.toFixed(2)}%)`;
}

export function buildDartCorpUrl(corpCode?: string | null): string | null {
  const code = corpCode?.trim();

  if (!code) {
    return null;
  }

  return `https://dart.fss.or.kr/html/search/SearchCompany_M2.html?textCrpCD=${encodeURIComponent(code)}`;
}

export function hasAnyStockStructureField(profile: {
  issuedStock?: string | null;
  outstandingShares?: string | null;
  treasuryStock?: string | null;
  declinedStock?: string | null;
} | null): boolean {
  if (!profile) {
    return false;
  }

  return Boolean(
    profile.issuedStock ||
      profile.outstandingShares ||
      profile.treasuryStock ||
      profile.declinedStock
  );
}
