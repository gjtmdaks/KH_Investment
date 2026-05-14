"use client";

import { useCallback, useEffect, useState } from "react";
import StockCandleChart, {
  type ChartCandle,
} from "@/app/components/stock/StockCandleChart";
import { chartPeriods } from "@/lib/stock/stockDetailConstants";
import type { ChartPeriodLabel } from "@/lib/stock/stockDetailTypes";
import {
  aggregateYearlyCandles,
  getApiPeriod,
  getChartDateRange,
  normalizeCandlePayload,
} from "@/lib/stock/stockChartCandles";

import styles from "./stockDetail.module.css";

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

  const loadCandles = useCallback(async () => {
    setChartLoading(true);
    setChartError(null);

    try {
      const { from, to } = getChartDateRange(activePeriod);
      const apiPeriod = getApiPeriod(activePeriod);
      const response = await fetchJson<unknown>(
        `/api/stocks/${stockCode}/candles?period=${apiPeriod}&from=${from}&to=${to}`
      );
      const normalizedCandles = normalizeCandlePayload(response);
      const nextCandles =
        activePeriod === "년"
          ? aggregateYearlyCandles(normalizedCandles)
          : normalizedCandles;

      setCandles(nextCandles);
    } catch {
      setCandles([]);
      setChartError("차트 데이터를 불러오지 못했습니다.");
    } finally {
      setChartLoading(false);
    }
  }, [activePeriod, fetchJson, stockCode]);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      void loadCandles();
    }, 0);

    return () => window.clearTimeout(timer);
  }, [loadCandles]);

  return (
    <div className={styles.chartCard}>
      <div className={styles.periods}>
        {chartPeriods.map((period) => (
          <button
            key={period}
            type="button"
            className={activePeriod === period ? styles.activePeriod : ""}
            onClick={() => setActivePeriod(period)}
          >
            {period}
          </button>
        ))}
      </div>
      <div className={styles.chartBody}>
        <StockCandleChart
          candles={candles}
          loading={chartLoading}
          error={chartError}
        />
      </div>
    </div>
  );
}
