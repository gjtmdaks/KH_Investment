"use client";

import { useLayoutEffect, useRef } from "react";
import {
  CandlestickSeries,
  ColorType,
  createChart,
  CrosshairMode,
  HistogramSeries,
  isBusinessDay,
  type Time,
  type CandlestickData,
  type HistogramData,
  type IChartApi,
  type ISeriesApi,
} from "lightweight-charts";
import styles from "./stockCandleChart.module.css";

export type ChartCandle = {
  date: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
};

type StockCandleChartProps = {
  candles: ChartCandle[];
  loading: boolean;
  error: string | null;
  emptyMessage?: string;
};

const CHART_HEIGHT = 360;

const CHART_INTERACTION_OPTIONS = {
  handleScroll: {
    mouseWheel: false,
    pressedMouseMove: false,
    horzTouchDrag: false,
    vertTouchDrag: false,
  },
  handleScale: {
    axisPressedMouseMove: false,
    axisDoubleClickReset: false,
    mouseWheel: false,
    pinch: false,
  },
  kineticScroll: {
    mouse: false,
    touch: false,
  },
} as const;

const CHART_SCALE_FONT_SIZE = 19;

function formatCrosshairKoreanDate(time: Time): string {
  if (isBusinessDay(time)) {
    const yy = time.year % 100;

    return `${yy}년 ${time.month}월 ${time.day}일`;
  }

  if (typeof time === "number" && Number.isFinite(time)) {
    const d = new Date(time * 1000);
    const yy = d.getFullYear() % 100;

    return `${yy}년 ${d.getMonth() + 1}월 ${d.getDate()}일`;
  }

  if (typeof time === "string") {
    const iso = /^(\d{4})-(\d{2})-(\d{2})$/.exec(time.trim());

    if (iso) {
      const y = Number(iso[1]);
      const month = Number(iso[2]);
      const day = Number(iso[3]);

      if (Number.isFinite(y) && Number.isFinite(month) && Number.isFinite(day)) {
        return `${y % 100}년 ${month}월 ${day}일`;
      }
    }
  }

  return String(time);
}

export default function StockCandleChart({
  candles,
  loading,
  error,
  emptyMessage = "표시할 차트 데이터가 없습니다.",
}: StockCandleChartProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const chartRef = useRef<IChartApi | null>(null);
  const candleSeriesRef = useRef<ISeriesApi<"Candlestick"> | null>(null);
  const volumeSeriesRef = useRef<ISeriesApi<"Histogram"> | null>(null);

  useLayoutEffect(() => {
    const container = containerRef.current;
    if (!container) {
      return;
    }

    let chart = chartRef.current;

    if (!chart) {
      chart = createChart(container, {
        layout: {
          background: { type: ColorType.Solid, color: "#0b1120" },
          textColor: "#94a3b8",
          fontSize: CHART_SCALE_FONT_SIZE,
        },
        localization: {
          locale: "ko-KR",
          timeFormatter: formatCrosshairKoreanDate,
        },
        grid: {
          vertLines: { color: "rgba(148, 163, 184, 0.08)" },
          horzLines: { color: "rgba(148, 163, 184, 0.08)" },
        },
        rightPriceScale: {
          borderColor: "rgba(148, 163, 184, 0.12)",
        },
        timeScale: {
          borderColor: "rgba(148, 163, 184, 0.12)",
          fixLeftEdge: true,
          fixRightEdge: true,
          lockVisibleTimeRangeOnResize: true,
          rightOffset: 0,
        },
        crosshair: {
          mode: CrosshairMode.Normal,
        },
        ...CHART_INTERACTION_OPTIONS,
        width: container.clientWidth,
        height: CHART_HEIGHT,
      });

      const candleSeries = chart.addSeries(CandlestickSeries, {
        upColor: "#fb7185",
        downColor: "#60a5fa",
        borderVisible: false,
        wickUpColor: "#fb7185",
        wickDownColor: "#60a5fa",
        priceFormat: {
          type: "price",
          precision: 0,
          minMove: 1,
        },
      });

      const volumeSeries = chart.addSeries(
        HistogramSeries,
        {
          priceFormat: { type: "volume" },
          priceScaleId: "",
        },
        1
      );

      volumeSeries.priceScale().applyOptions({
        scaleMargins: {
          top: 0.8,
          bottom: 0,
        },
      });

      chartRef.current = chart;
      candleSeriesRef.current = candleSeries;
      volumeSeriesRef.current = volumeSeries;
    }

    const candleSeries = candleSeriesRef.current;
    const volumeSeries = volumeSeriesRef.current;

    if (!candleSeries || !volumeSeries || !chart) {
      return;
    }

    if (loading || error || candles.length === 0) {
      candleSeries.setData([]);
      volumeSeries.setData([]);
      return;
    }

    const candleData: CandlestickData[] = candles.map((candle) => ({
      time: candle.date,
      open: candle.open,
      high: candle.high,
      low: candle.low,
      close: candle.close,
    }));

    const volumeData: HistogramData[] = candles.map((candle) => ({
      time: candle.date,
      value: candle.volume,
      color:
        candle.close >= candle.open
          ? "rgba(251, 113, 133, 0.5)"
          : "rgba(96, 165, 250, 0.5)",
    }));

    candleSeries.setData(candleData);
    volumeSeries.setData(volumeData);
    chart.applyOptions({
      width: container.clientWidth,
      height: CHART_HEIGHT,
      layout: {
        fontSize: CHART_SCALE_FONT_SIZE,
      },
      localization: {
        locale: "ko-KR",
        timeFormatter: formatCrosshairKoreanDate,
      },
      crosshair: {
        mode: CrosshairMode.Normal,
      },
      ...CHART_INTERACTION_OPTIONS,
    });
    chart.timeScale().setVisibleLogicalRange({
      from: 0,
      to: Math.max(candleData.length - 1, 0),
    });
  }, [candles, error, loading]);

  useLayoutEffect(() => {
    const container = containerRef.current;
    const chart = chartRef.current;

    if (!container || !chart) {
      return;
    }

    const observer = new ResizeObserver((entries) => {
      const entry = entries[0];
      if (!entry) {
        return;
      }

      chart.applyOptions({
        width: entry.contentRect.width,
        height: CHART_HEIGHT,
      });
    });

    observer.observe(container);

    return () => {
      observer.disconnect();
    };
  }, []);

  useLayoutEffect(() => {
    return () => {
      chartRef.current?.remove();
      chartRef.current = null;
      candleSeriesRef.current = null;
      volumeSeriesRef.current = null;
    };
  }, []);

  const showOverlay = loading || Boolean(error) || candles.length === 0;

  return (
    <div className={styles.chartRoot}>
      <div ref={containerRef} className={styles.chartCanvas} />
      {showOverlay ? (
        <div className={styles.overlay}>
          {loading ? <strong>차트 데이터를 불러오는 중입니다.</strong> : null}
          {!loading && error ? (
            <>
              <strong>차트를 불러오지 못했습니다.</strong>
              <span>{error}</span>
            </>
          ) : null}
          {!loading && !error && candles.length === 0 ? (
            <strong>{emptyMessage}</strong>
          ) : null}
        </div>
      ) : null}
    </div>
  );
}
