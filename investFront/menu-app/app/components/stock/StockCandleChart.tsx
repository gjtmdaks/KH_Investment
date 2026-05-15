"use client";

import { useLayoutEffect, useRef, useState, startTransition } from "react";
import {
  CandlestickSeries,
  ColorType,
  createChart,
  CrosshairMode,
  HistogramSeries,
  isBusinessDay,
  type HistogramData,
  type IChartApi,
  type ISeriesApi,
  type MouseEventParams,
  type OhlcData,
  type Time,
  type CandlestickData,
} from "lightweight-charts";
import { formatNumber } from "@/lib/stock/stockDetailFormat";
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
  viewResetKey: string;
};

const CHART_HEIGHT = 360;

const CHART_INTERACTION_OPTIONS = {
  handleScroll: {
    mouseWheel: true,
    pressedMouseMove: true,
    horzTouchDrag: true,
    vertTouchDrag: false,
  },
  handleScale: {
    axisPressedMouseMove: true,
    axisDoubleClickReset: true,
    mouseWheel: true,
    pinch: true,
  },
  kineticScroll: {
    mouse: true,
    touch: true,
  },
} as const;

const CHART_SCALE_FONT_SIZE = 19;

const TOOLTIP_WIDTH = 208;
const TOOLTIP_HEIGHT = 132;
const TOOLTIP_PAD = 10;

type CrosshairTooltipState = {
  left: number;
  top: number;
  dateLabel: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number | null;
};

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

function formatTooltipPrice(n: number): string {
  return Math.round(n).toLocaleString("ko-KR");
}

export default function StockCandleChart({
  candles,
  loading,
  error,
  emptyMessage = "표시할 차트 데이터가 없습니다.",
  viewResetKey,
}: StockCandleChartProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const chartRef = useRef<IChartApi | null>(null);
  const candleSeriesRef = useRef<ISeriesApi<"Candlestick"> | null>(null);
  const volumeSeriesRef = useRef<ISeriesApi<"Histogram"> | null>(null);
  const crosshairHandlerRef = useRef<
    ((param: MouseEventParams<Time>) => void) | null
  >(null);
  const appliedFitKeyRef = useRef<string | null>(null);

  const [crosshairTooltip, setCrosshairTooltip] =
    useState<CrosshairTooltipState | null>(null);

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
          fixLeftEdge: false,
          fixRightEdge: false,
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

      const crosshairHandler = (param: MouseEventParams<Time>) => {
        const cs = candleSeriesRef.current;
        const vs = volumeSeriesRef.current;
        const el = containerRef.current;

        if (!cs || !el) {
          setCrosshairTooltip(null);

          return;
        }

        if (!param.point || param.time === undefined) {
          setCrosshairTooltip(null);

          return;
        }

        const raw = param.seriesData.get(cs);
        if (!raw || typeof raw !== "object" || !("open" in raw)) {
          setCrosshairTooltip(null);

          return;
        }

        const ohlc = raw as OhlcData<Time>;
        let volume: number | null = null;
        if (vs) {
          const hv = param.seriesData.get(vs);
          if (hv && typeof hv === "object" && "value" in hv) {
            volume = (hv as HistogramData<Time>).value;
          }
        }

        const cw = el.clientWidth;
        const ch = el.clientHeight;
        let left = param.point.x + TOOLTIP_PAD;
        let top = param.point.y + TOOLTIP_PAD;

        if (left + TOOLTIP_WIDTH > cw) {
          left = param.point.x - TOOLTIP_WIDTH - TOOLTIP_PAD;
        }
        if (top + TOOLTIP_HEIGHT > ch) {
          top = param.point.y - TOOLTIP_HEIGHT - TOOLTIP_PAD;
        }

        left = Math.max(
          TOOLTIP_PAD,
          Math.min(left, cw - TOOLTIP_WIDTH - TOOLTIP_PAD)
        );
        top = Math.max(
          TOOLTIP_PAD,
          Math.min(top, ch - TOOLTIP_HEIGHT - TOOLTIP_PAD)
        );

        setCrosshairTooltip({
          left,
          top,
          dateLabel: formatCrosshairKoreanDate(param.time),
          open: ohlc.open,
          high: ohlc.high,
          low: ohlc.low,
          close: ohlc.close,
          volume,
        });
      };

      crosshairHandlerRef.current = crosshairHandler;
      chart.subscribeCrosshairMove(crosshairHandler);
    }

    const candleSeries = candleSeriesRef.current;
    const volumeSeries = volumeSeriesRef.current;

    if (!candleSeries || !volumeSeries || !chart) {
      return;
    }

    if (loading || error || candles.length === 0) {
      candleSeries.setData([]);
      volumeSeries.setData([]);
      startTransition(() => {
        setCrosshairTooltip(null);
      });

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
      timeScale: {
        fixLeftEdge: false,
        fixRightEdge: false,
        lockVisibleTimeRangeOnResize: true,
        rightOffset: 0,
      },
      ...CHART_INTERACTION_OPTIONS,
    });
  }, [candles, error, loading]);

  useLayoutEffect(() => {
    const chart = chartRef.current;
    if (error || (!loading && candles.length === 0)) {
      appliedFitKeyRef.current = null;
    }
    if (!chart || loading || error || candles.length === 0) {
      return;
    }
    if (appliedFitKeyRef.current === viewResetKey) {
      return;
    }
    appliedFitKeyRef.current = viewResetKey;
    chart.timeScale().fitContent();
  }, [candles.length, error, loading, viewResetKey]);

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
      const chart = chartRef.current;
      const handler = crosshairHandlerRef.current;
      if (chart && handler) {
        chart.unsubscribeCrosshairMove(handler);
      }
      crosshairHandlerRef.current = null;
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
      {crosshairTooltip && !showOverlay ? (
        <div
          className={styles.crosshairTooltip}
          style={{
            left: crosshairTooltip.left,
            top: crosshairTooltip.top,
            width: TOOLTIP_WIDTH,
          }}
          role="status"
          aria-live="polite"
        >
          <div className={styles.crosshairTooltipDate}>
            {crosshairTooltip.dateLabel}
          </div>
          <div className={styles.crosshairTooltipGrid}>
            <span className={styles.crosshairTooltipLabel}>시</span>
            <span className={styles.crosshairTooltipNum}>
              {formatTooltipPrice(crosshairTooltip.open)}
            </span>
            <span className={styles.crosshairTooltipLabel}>고</span>
            <span className={styles.crosshairTooltipNum}>
              {formatTooltipPrice(crosshairTooltip.high)}
            </span>
            <span className={styles.crosshairTooltipLabel}>저</span>
            <span className={styles.crosshairTooltipNum}>
              {formatTooltipPrice(crosshairTooltip.low)}
            </span>
            <span className={styles.crosshairTooltipLabel}>종</span>
            <span className={styles.crosshairTooltipNum}>
              {formatTooltipPrice(crosshairTooltip.close)}
            </span>
          </div>
          <div className={styles.crosshairTooltipVolumeRow}>
            <span className={styles.crosshairTooltipLabel}>거래량</span>
            <span className={styles.crosshairTooltipNum}>
              {crosshairTooltip.volume != null
                ? formatNumber(String(Math.round(crosshairTooltip.volume)))
                : "-"}
            </span>
          </div>
        </div>
      ) : null}
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
