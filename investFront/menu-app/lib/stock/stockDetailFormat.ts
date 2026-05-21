export function parseNumeric(value?: string | null) {
  const numeric = Number(String(value ?? "").replaceAll(",", ""));

  return Number.isFinite(numeric) ? numeric : null;
}

function formatScaledAmount(value: number, decimals: number) {
  return value.toLocaleString("ko-KR", {
    minimumFractionDigits: 0,
    maximumFractionDigits: decimals,
  });
}

export function formatNumber(value?: string | null) {
  const numeric = parseNumeric(value);

  if (numeric === null || value === null || value === undefined || value === "") {
    return "-";
  }

  return numeric.toLocaleString("ko-KR");
}

function formatKoreanLargeAmount(
  numeric: number,
  unitSuffix: string
) {
  const ONE_EOK = 100_000_000;
  const ONE_JO = 1_000_000_000_000;

  if (numeric >= ONE_JO) {
    return `${formatScaledAmount(numeric / ONE_JO, 1)}조${unitSuffix}`;
  }

  if (numeric >= ONE_EOK) {
    const decimals = numeric >= ONE_EOK * 100 ? 0 : 1;
    return `${formatScaledAmount(numeric / ONE_EOK, decimals)}억${unitSuffix}`;
  }

  if (numeric >= 10_000) {
    return `${Math.round(numeric / 10_000).toLocaleString("ko-KR")}만${unitSuffix}`;
  }

  return `${numeric.toLocaleString("ko-KR")}${unitSuffix}`;
}

function parseKoreanLargeInput(value?: string | number | null) {
  const numeric = typeof value === "number" ? value : parseNumeric(value);

  if (numeric === null) {
    return null;
  }

  return numeric;
}

export function formatKoreanLargeWon(value?: string | number | null) {
  const numeric = parseKoreanLargeInput(value);

  if (numeric === null) {
    return "-";
  }

  return formatKoreanLargeAmount(numeric, "원");
}

export function formatKoreanLargeShares(value?: string | number | null) {
  const numeric = parseKoreanLargeInput(value);

  if (numeric === null) {
    return "-";
  }

  return formatKoreanLargeAmount(numeric, "");
}

export function formatSignedKoreanLargeShares(value?: number | null) {
  if (value === null || value === undefined) {
    return "-";
  }

  const sign = value > 0 ? "+" : value < 0 ? "-" : "";
  const absText = formatKoreanLargeShares(Math.abs(value));

  if (absText === "-") {
    return "-";
  }

  return `${sign}${absText}`;
}

export function formatSignedNumber(value?: number | null) {
  if (value === null || value === undefined) {
    return "-";
  }

  const sign = value > 0 ? "+" : "";
  return `${sign}${value.toLocaleString("ko-KR")}`;
}

export function formatInvestorTradeDate(tradeDate?: string | null) {
  if (!tradeDate || tradeDate.length < 8) {
    return "-";
  }

  const yy = tradeDate.slice(2, 4);
  const mm = tradeDate.slice(4, 6);
  const dd = tradeDate.slice(6, 8);

  return `${yy}.${mm}.${dd}`;
}

export function formatWon(value?: string | null) {
  const formatted = formatNumber(value);

  return formatted === "-" ? formatted : `${formatted}원`;
}

export function formatPercent(value?: string | null) {
  const numeric = Number(String(value ?? "").replaceAll(",", ""));

  if (!Number.isFinite(numeric) || value === null || value === undefined || value === "") {
    return "-";
  }

  return `${numeric > 0 ? "+" : ""}${numeric.toFixed(2)}%`;
}

export function formatPlainPercent(value?: string | null) {
  const numeric = Number(String(value ?? "").replaceAll(",", ""));

  if (!Number.isFinite(numeric) || value === null || value === undefined || value === "") {
    return "-";
  }

  return `${numeric.toFixed(2)}%`;
}

export function formatExecutionStrength(value?: string | null) {
  const numeric = parseNumeric(value);

  if (numeric === null) {
    return "-";
  }

  return `${numeric.toFixed(2)}%`;
}

export function formatChange(value?: string | null) {
  const numeric = Number(String(value ?? "").replaceAll(",", ""));

  if (!Number.isFinite(numeric) || value === null || value === undefined || value === "") {
    return "-";
  }

  return `${numeric > 0 ? "+" : ""}${numeric.toLocaleString("ko-KR")}원`;
}

export function stripHtml(value: string) {
  return value.replace(/<[^>]*>/g, "").replaceAll("&quot;", "\"").replaceAll("&amp;", "&");
}
