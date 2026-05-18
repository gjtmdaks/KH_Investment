"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import StockCandleChart, {
  type ChartCandle,
} from "@/app/components/stock/StockCandleChart";
import { chartPeriods } from "@/lib/stock/stockDetailConstants";
import type { ChartPeriodLabel } from "@/lib/stock/stockDetailTypes";
import {
  aggregateYearlyCandles,
  getApiPeriod,
  getChartZoomProfile,
  getChartDateRange,
  type ChartZoomProfile,
  getMinuteIntervalMinutes,
  getPreviousTradingDay,
  getSeoulIsoDate,
  isMinuteChartPeriod,
  MAX_MINUTE_SCROLL_TRADING_DAYS,
  mergeMinuteCandles,
  normalizeCandlePayload,
} from "@/lib/stock/stockChartCandles";

import styles from "./css/stockDetailChartShell.module.css";

const HOLIDAY_RETRY_LIMIT = 3;
const PERIOD_CANDLE_RETRY_DELAY_MS = 2_500;

type CachedMinuteChart = {
  kind: "minute";
  candles: ChartCandle[];
  earliestTradeDate: string;
  loadedTradingDays: number;
  hasMoreOlder: boolean;
};

type CachedPeriodChart = {
  kind: "period";
  candles: ChartCandle[];
};

type CachedChartState = CachedMinuteChart | CachedPeriodChart;

function getChartCacheKey(stockCode: string, period: ChartPeriodLabel) {
  if (isMinuteChartPeriod(period)) {
    return `${stockCode}:M${getMinuteIntervalMinutes(period)}`;
  }

  return `${stockCode}:${period}`;
}

export function StockDetailChartShell({
  stockCode,
  fetchJson,
}: {
  stockCode: string;
  fetchJson: <T>(path: string) => Promise<T>;
}) {
  const [activePeriod, setActivePeriod] = useState<ChartPeriodLabel>("일");
  const [candles, setCandles] = useState<ChartCandle[]>([]);
  const [chartLoading, setChartLoading] = useState(true);
  const [chartError, setChartError] = useState<string | null>(null);
  const [loadingOlder, setLoadingOlder] = useState(false);
  const [hasMoreOlder, setHasMoreOlder] = useState(true);
  const [earliestTradeDate, setEarliestTradeDate] = useState<string | null>(
    null
  );

  const earliestTradeDateRef = useRef<string | null>(null);
  const loadedTradingDaysRef = useRef(0);
  const loadingOlderRef = useRef(false);
  const hasMoreOlderRef = useRef(true);
  const chartCacheRef = useRef<Map<string, CachedChartState>>(new Map());
  const activePeriodRef = useRef(activePeriod);
  const candlesRef = useRef(candles);

  candlesRef.current = candles;

  const persistChartCache = useCallback(
    (period: ChartPeriodLabel) => {
      const snapshot = candlesRef.current;

      if (snapshot.length === 0) {
        return;
      }

      const key = getChartCacheKey(stockCode, period);

      if (isMinuteChartPeriod(period)) {
        const tradeDate =
          earliestTradeDateRef.current ?? getChartDateRange(period).from;

        chartCacheRef.current.set(key, {
          kind: "minute",
          candles: snapshot,
          earliestTradeDate: tradeDate,
          loadedTradingDays: loadedTradingDaysRef.current,
          hasMoreOlder: hasMoreOlderRef.current,
        });

        return;
      }

      chartCacheRef.current.set(key, {
        kind: "period",
        candles: snapshot,
      });
    },
    [stockCode]
  );

  const restoreMinuteCache = useCallback((entry: CachedMinuteChart) => {
    setCandles(entry.candles);
    earliestTradeDateRef.current = entry.earliestTradeDate;
    loadedTradingDaysRef.current = entry.loadedTradingDays;
    hasMoreOlderRef.current = entry.hasMoreOlder;
    setEarliestTradeDate(entry.earliestTradeDate);
    setHasMoreOlder(entry.hasMoreOlder);
  }, []);

  useEffect(() => {
    chartCacheRef.current.clear();
  }, [stockCode]);

  useEffect(() => {
    const previousPeriod = activePeriodRef.current;

    if (previousPeriod !== activePeriod) {
      persistChartCache(previousPeriod);
      activePeriodRef.current = activePeriod;
    }
  }, [activePeriod, persistChartCache]);

  const resetMinuteScrollState = useCallback((tradeDate: string) => {
    earliestTradeDateRef.current = tradeDate;
    loadedTradingDaysRef.current = 1;
    hasMoreOlderRef.current = true;
    setEarliestTradeDate(tradeDate);
    setHasMoreOlder(true);
  }, []);

  const loadCandles = useCallback(async () => {
    const cacheKey = getChartCacheKey(stockCode, activePeriod);
    const cached = chartCacheRef.current.get(cacheKey);

    if (cached) {
      if (cached.kind === "minute") {
        restoreMinuteCache(cached);
      } else {
        setCandles(cached.candles);
        hasMoreOlderRef.current = false;
        setHasMoreOlder(false);
      }

      setChartLoading(false);
      setChartError(null);

      return;
    }

    setChartLoading(true);
    setChartError(null);

    try {
      const minuteMode = isMinuteChartPeriod(activePeriod);

      if (minuteMode) {
        const tradeDate = getChartDateRange(activePeriod).from;
        const intervalMinutes = getMinuteIntervalMinutes(activePeriod);
        const response = await fetchJson<unknown>(
          `/api/stocks/${stockCode}/minute-candles?intervalMinutes=${intervalMinutes}&tradeDate=${tradeDate}`
        );
        const normalizedCandles = normalizeCandlePayload(response);

        setCandles(normalizedCandles);
        resetMinuteScrollState(tradeDate);
        chartCacheRef.current.set(cacheKey, {
          kind: "minute",
          candles: normalizedCandles,
          earliestTradeDate: tradeDate,
          loadedTradingDays: 1,
          hasMoreOlder: true,
        });
      } else {
        const { from, to } = getChartDateRange(activePeriod);
        const apiPeriod = getApiPeriod(activePeriod);
        const candlesPath = `/api/stocks/${stockCode}/candles?period=${apiPeriod}&from=${from}&to=${to}`;

        const fetchPeriodCandles = async () => {
          const response = await fetchJson<unknown>(candlesPath);
          const normalizedCandles = normalizeCandlePayload(response);

          return activePeriod === "년"
            ? aggregateYearlyCandles(normalizedCandles)
            : normalizedCandles;
        };

        let nextCandles = await fetchPeriodCandles();

        if (nextCandles.length === 0) {
          await new Promise<void>((resolve) => {
            window.setTimeout(resolve, PERIOD_CANDLE_RETRY_DELAY_MS);
          });
          nextCandles = await fetchPeriodCandles();
        }

        setCandles(nextCandles);
        hasMoreOlderRef.current = false;
        setHasMoreOlder(false);

        if (nextCandles.length > 0) {
          chartCacheRef.current.set(cacheKey, {
            kind: "period",
            candles: nextCandles,
          });
        }
      }
    } catch {
      setCandles([]);
      setChartError("차트 데이터를 불러오지 못했습니다.");
    } finally {
      setChartLoading(false);
    }
  }, [
    activePeriod,
    fetchJson,
    persistChartCache,
    resetMinuteScrollState,
    restoreMinuteCache,
    stockCode,
  ]);

  const loadOlderCandles = useCallback(async () => {
    if (
      !isMinuteChartPeriod(activePeriod) ||
      loadingOlderRef.current ||
      !hasMoreOlderRef.current
    ) {
      return;
    }

    if (loadedTradingDaysRef.current >= MAX_MINUTE_SCROLL_TRADING_DAYS) {
      hasMoreOlderRef.current = false;
      setHasMoreOlder(false);

      return;
    }

    const anchor = earliestTradeDateRef.current ?? getSeoulIsoDate();
    const intervalMinutes = getMinuteIntervalMinutes(activePeriod);

    loadingOlderRef.current = true;
    setLoadingOlder(true);

    try {
      let candidate = getPreviousTradingDay(anchor);
      let merged: ChartCandle[] | null = null;
      let resolvedEarliest = anchor;

      for (let attempt = 0; attempt < HOLIDAY_RETRY_LIMIT; attempt++) {
        const response = await fetchJson<unknown>(
          `/api/stocks/${stockCode}/minute-candles?intervalMinutes=${intervalMinutes}&tradeDate=${candidate}`
        );
        const batch = normalizeCandlePayload(response);

        if (batch.length > 0) {
          merged = batch;
          resolvedEarliest = candidate;
          break;
        }

        candidate = getPreviousTradingDay(candidate);
      }

      if (!merged || merged.length === 0) {
        hasMoreOlderRef.current = false;
        setHasMoreOlder(false);

        return;
      }

      setCandles((current) => {
        const next = mergeMinuteCandles(merged, current);

        earliestTradeDateRef.current = resolvedEarliest;
        loadedTradingDaysRef.current += 1;

        const reachedLimit =
          loadedTradingDaysRef.current >= MAX_MINUTE_SCROLL_TRADING_DAYS;

        if (reachedLimit) {
          hasMoreOlderRef.current = false;
        }

        chartCacheRef.current.set(getChartCacheKey(stockCode, activePeriod), {
          kind: "minute",
          candles: next,
          earliestTradeDate: resolvedEarliest,
          loadedTradingDays: loadedTradingDaysRef.current,
          hasMoreOlder: !reachedLimit,
        });

        return next;
      });
      setEarliestTradeDate(resolvedEarliest);
      setHasMoreOlder(loadedTradingDaysRef.current < MAX_MINUTE_SCROLL_TRADING_DAYS);
    } catch {
      hasMoreOlderRef.current = false;
      setHasMoreOlder(false);
    } finally {
      loadingOlderRef.current = false;
      setLoadingOlder(false);
    }
  }, [activePeriod, fetchJson, stockCode]);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      void loadCandles();
    }, 0);

    return () => window.clearTimeout(timer);
  }, [loadCandles]);

  const intradayMode = isMinuteChartPeriod(activePeriod);
  const nextOlderTradeDate =
    earliestTradeDate != null
      ? getPreviousTradingDay(earliestTradeDate)
      : null;

  return (
    <ChartShellLayout
      activePeriod={activePeriod}
      setActivePeriod={setActivePeriod}
      candles={candles}
      chartLoading={chartLoading}
      chartError={chartError}
      intradayMode={intradayMode}
      loadingOlder={loadingOlder}
      hasMoreOlder={hasMoreOlder}
      nextOlderTradeDate={nextOlderTradeDate}
      onLoadOlder={() => {
        void loadOlderCandles();
      }}
      viewResetKey={`${stockCode}-${activePeriod}`}
      zoomProfile={getChartZoomProfile(activePeriod)}
    />
  );
}

function ChartShellLayout(props: {
  activePeriod: ChartPeriodLabel;
  setActivePeriod: (period: ChartPeriodLabel) => void;
  candles: ChartCandle[];
  chartLoading: boolean;
  chartError: string | null;
  intradayMode: boolean;
  loadingOlder: boolean;
  hasMoreOlder: boolean;
  nextOlderTradeDate: string | null;
  onLoadOlder: () => void;
  viewResetKey: string;
  zoomProfile: ChartZoomProfile;
}) {
  const showLoadOlderButton =
    props.intradayMode && props.hasMoreOlder && !props.chartLoading;

  return (
    <div className={styles.chartCard}>
      <div className={styles.periods}>
        {chartPeriods.map((period) => (
          <button
            key={period}
            type="button"
            className={props.activePeriod === period ? styles.activePeriod : ""}
            onClick={() => props.setActivePeriod(period)}
          >
            {period}
          </button>
        ))}
      </div>
      <div className={styles.chartBody}>
        {showLoadOlderButton ? (
          <div className={styles.loadOlderOverlay}>
            <button
              type="button"
              className={styles.loadOlderButton}
              disabled={props.loadingOlder}
              onClick={props.onLoadOlder}
            >
              {props.loadingOlder
                ? "전일 데이터 불러오는 중…"
                : formatLoadOlderLabel(props.nextOlderTradeDate)}
            </button>
          </div>
        ) : null}
        <StockCandleChart
          candles={props.candles}
          loading={props.chartLoading}
          error={props.chartError}
          viewResetKey={props.viewResetKey}
          zoomProfile={props.zoomProfile}
          intradayMode={props.intradayMode}
        />
      </div>
    </div>
  );
}

function formatLoadOlderLabel(nextTradeDate: string | null) {
  if (!nextTradeDate) {
    return "전일 데이터 추가";
  }

  const [, month, day] = nextTradeDate.split("-");

  return `전일(${Number(month)}/${Number(day)}) 데이터 추가`;
}
