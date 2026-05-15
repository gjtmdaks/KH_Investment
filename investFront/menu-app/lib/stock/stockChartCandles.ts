import type { ChartCandle } from "@/app/components/stock/StockCandleChart";

import type { ChartPeriodLabel } from "@/lib/stock/stockDetailTypes";

export function isMinuteChartPeriod(period: ChartPeriodLabel) {
  return (
    period === "1분" ||
    period === "15분" ||
    period === "30분" ||
    period === "60분"
  );
}

export function getMinuteIntervalMinutes(period: ChartPeriodLabel) {
  switch (period) {
    case "1분":
      return 1;
    case "15분":
      return 15;
    case "30분":
      return 30;
    case "60분":
      return 60;
    default:
      return 1;
  }
}

export function getSeoulIsoDate() {
  return new Date().toLocaleDateString("sv-SE", { timeZone: "Asia/Seoul" });
}

export function getApiPeriod(period: ChartPeriodLabel) {
  if (isMinuteChartPeriod(period)) {
    throw new Error("분봉 주기는 getApiPeriod를 사용하지 않습니다.");
  }

  if (period === "일") {
    return "D";
  }

  if (period === "주") {
    return "W";
  }

  return "M";
}

export function getChartDateRange(period: ChartPeriodLabel) {
  if (isMinuteChartPeriod(period)) {
    const d = getSeoulIsoDate();

    return {
      from: d,
      to: d,
    };
  }

  const to = new Date();
  const from = new Date(to);

  if (period === "일") {
    from.setDate(from.getDate() - 120);
  } else if (period === "주") {
    from.setFullYear(from.getFullYear() - 3);
  } else {
    from.setFullYear(from.getFullYear() - 10);
  }

  return {
    from: formatIsoDate(from),
    to: formatIsoDate(to),
  };
}

function formatIsoDate(value: Date) {
  const year = value.getFullYear();
  const month = String(value.getMonth() + 1).padStart(2, "0");
  const day = String(value.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
}

export function normalizeCandlePayload(payload: unknown): ChartCandle[] {
  if (!payload || typeof payload !== "object") {
    return [];
  }

  const raw = payload as {
    success?: boolean;
    message?: string | null;
    candles?: unknown;
    data?: {
      candles?: unknown;
    } | null;
  };

  if (raw.success === false) {
    throw new Error(raw.message || "차트 데이터를 불러오지 못했습니다.");
  }

  const source = Array.isArray(raw.candles)
    ? raw.candles
    : Array.isArray(raw.data?.candles)
      ? raw.data.candles
      : [];

  return source
    .filter((item): item is Record<string, unknown> => {
      return Boolean(item) && typeof item === "object";
    })
    .map((item) => normalizeCandle(item))
    .filter((item) => item.date.length > 0);
}

function normalizeCandle(item: Record<string, unknown>): ChartCandle {
  return {
    date: String(item.date ?? ""),
    open: toNumber(item.open),
    high: toNumber(item.high),
    low: toNumber(item.low),
    close: toNumber(item.close),
    volume: toNumber(item.volume),
  };
}

function toNumber(value: unknown) {
  const numeric = Number(value);

  return Number.isFinite(numeric) ? numeric : 0;
}

export function aggregateYearlyCandles(candles: ChartCandle[]) {
  const grouped = new Map<number, ChartCandle[]>();

  for (const candle of candles) {
    const year = Number(candle.date.slice(0, 4));
    if (!Number.isFinite(year)) {
      continue;
    }

    const bucket = grouped.get(year);
    if (bucket) {
      bucket.push(candle);
    } else {
      grouped.set(year, [candle]);
    }
  }

  return [...grouped.entries()]
    .sort(([leftYear], [rightYear]) => leftYear - rightYear)
    .map(([year, items]) => {
      const open = items[0]?.open ?? 0;
      const high = Math.max(...items.map((item) => item.high));
      const low = Math.min(...items.map((item) => item.low));
      const close = items[items.length - 1]?.close ?? 0;
      const volume = items.reduce((sum, item) => sum + item.volume, 0);

      return {
        date: `${year}-12-31`,
        open,
        high,
        low,
        close,
        volume,
      };
    });
}
