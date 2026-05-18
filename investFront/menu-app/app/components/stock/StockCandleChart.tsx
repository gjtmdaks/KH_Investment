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
import type { ChartZoomProfile } from "@/lib/stock/stockChartCandles";
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
  zoomProfile?: ChartZoomProfile;
  intradayMode?: boolean;
};

const CHART_HEIGHT = 360;

const CANDLE_UP_COLOR = "#fb7185";
const CANDLE_DOWN_COLOR = "#60a5fa";
const CANDLE_UP_VOLUME_COLOR = "rgba(251, 113, 133, 0.5)";
const CANDLE_DOWN_VOLUME_COLOR = "rgba(96, 165, 250, 0.5)";

function isCandleUp(candle: ChartCandle) {
  return candle.close >= candle.open;
}

function getCandleColors(candle: ChartCandle) {
  const up = isCandleUp(candle);

  return {
    color: up ? CANDLE_UP_COLOR : CANDLE_DOWN_COLOR,
    volumeColor: up ? CANDLE_UP_VOLUME_COLOR : CANDLE_DOWN_VOLUME_COLOR,
  };
}

const CANDLESTICK_STYLE = {
  upColor: CANDLE_UP_COLOR,
  downColor: CANDLE_DOWN_COLOR,
  borderUpColor: CANDLE_UP_COLOR,
  borderDownColor: CANDLE_DOWN_COLOR,
  borderVisible: false,
  wickUpColor: CANDLE_UP_COLOR,
  wickDownColor: CANDLE_DOWN_COLOR,
  priceFormat: {
    type: "price" as const,
    precision: 0,
    minMove: 1,
  },
};

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
    mouse: false,
    touch: false,
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

const seoulIntradayLabelFormatter = new Intl.DateTimeFormat("ko-KR", {
  timeZone: "Asia/Seoul",
  year: "2-digit",
  month: "numeric",
  day: "numeric",
  hour: "2-digit",
  minute: "2-digit",
  hour12: false,
});

function candleTimeToChartTime(candle: ChartCandle): Time {
  const trimmed = candle.date.trim();

  if (/^\d{4}-\d{2}-\d{2}$/.test(trimmed)) {
    return trimmed;
  }

  const parsed = Date.parse(trimmed);

  if (Number.isFinite(parsed)) {
    return Math.floor(parsed / 1000) as Time;
  }

  return trimmed as Time;
}

function formatCrosshairLabel(time: Time, intradayMode: boolean): string {
  if (isBusinessDay(time)) {
    const yy = time.year % 100;

    return `${yy}년 ${time.month}월 ${time.day}일`;
  }

  if (typeof time === "number" && Number.isFinite(time)) {
    if (intradayMode) {
      return seoulIntradayLabelFormatter.format(new Date(time * 1000));
    }

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

    const ms = Date.parse(time);

    if (Number.isFinite(ms)) {
      if (intradayMode) {
        return seoulIntradayLabelFormatter.format(new Date(ms));
      }

      const d = new Date(ms);
      const yy = d.getFullYear() % 100;

      return `${yy}년 ${d.getMonth() + 1}월 ${d.getDate()}일`;
    }
  }

  return String(time);
}

function formatAxisTime(time: Time, intradayMode: boolean): string {
  return formatCrosshairLabel(time, intradayMode);
}

function formatTooltipPrice(n: number): string {
  return Math.round(n).toLocaleString("ko-KR");
}

function applyInitialZoom(chart: IChartApi, profile: ChartZoomProfile) {
  const timeScale = chart.timeScale();
  timeScale.fitContent();

  const range = timeScale.getVisibleLogicalRange();

  if (!range) {
    return;
  }

  const span = range.to - range.from;

  if (span <= 0) {
    return;
  }

  if (profile === "minute") {
    timeScale.setVisibleLogicalRange({
      from: range.from - span * 0.28,
      to: range.to + span * 0.06,
    });

    return;
  }

  if (profile === "longTerm") {
    const zoomInFrom = span * 0.42;

    timeScale.setVisibleLogicalRange({
      from: range.from + zoomInFrom,
      to: range.to,
    });
  }
}

export default function StockCandleChart({
  candles,
  loading,
  error,
  emptyMessage = "표시할 차트 데이터가 없습니다.",
  viewResetKey,
  zoomProfile = "daily",
  intradayMode = false,
}: StockCandleChartProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const chartRef = useRef<IChartApi | null>(null);
  const candleSeriesRef = useRef<ISeriesApi<"Candlestick"> | null>(null);
  const volumeSeriesRef = useRef<ISeriesApi<"Histogram"> | null>(null);
  const crosshairHandlerRef = useRef<
    ((param: MouseEventParams<Time>) => void) | null
  >(null);
  const appliedFitKeyRef = useRef<string | null>(null);
  const forceInitialZoomRef = useRef(false);
  const intradayModeRef = useRef(intradayMode);
  const zoomProfileRef = useRef(zoomProfile);
  const prevCandlesLengthRef = useRef(0);

  intradayModeRef.current = intradayMode;
  zoomProfileRef.current = zoomProfile;

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
          timeFormatter: (t: Time) =>
            formatAxisTime(t, intradayModeRef.current),
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
          timeVisible: intradayModeRef.current,
          secondsVisible: false,
        },
        crosshair: {
          mode: intradayModeRef.current
            ? CrosshairMode.MagnetOHLC
            : CrosshairMode.Normal,
        },
        ...CHART_INTERACTION_OPTIONS,
        width: container.clientWidth,
        height: CHART_HEIGHT,
      });

      const candleSeries = chart.addSeries(CandlestickSeries, CANDLESTICK_STYLE);

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
          dateLabel: formatCrosshairLabel(
            param.time,
            intradayModeRef.current
          ),
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
      prevCandlesLengthRef.current = 0;
      startTransition(() => {
        setCrosshairTooltip(null);
      });

      return;
    }

    const previousLength = prevCandlesLengthRef.current;
    const isPrepend = candles.length > previousLength && previousLength > 0;
    const prependOffset = isPrepend ? candles.length - previousLength : 0;

    const candleData: CandlestickData[] = candles.map((candle) => {
      const { color } = getCandleColors(candle);

      return {
        time: candleTimeToChartTime(candle),
        open: candle.open,
        high: candle.high,
        low: candle.low,
        close: candle.close,
        color,
        borderColor: color,
        wickColor: color,
      };
    });

    const volumeData: HistogramData[] = candles.map((candle) => {
      const { volumeColor } = getCandleColors(candle);

      return {
        time: candleTimeToChartTime(candle),
        value: candle.volume,
        color: volumeColor,
      };
    });

    const timeScale = chart.timeScale();
    const logicalRange = isPrepend ? timeScale.getVisibleLogicalRange() : null;

    candleSeries.applyOptions(CANDLESTICK_STYLE);
    candleSeries.setData(candleData);
    volumeSeries.setData(volumeData);

    if (isPrepend && logicalRange && prependOffset > 0) {
      timeScale.setVisibleLogicalRange({
        from: logicalRange.from + prependOffset,
        to: logicalRange.to + prependOffset,
      });
    }

    prevCandlesLengthRef.current = candles.length;
    chart.applyOptions({
      width: container.clientWidth,
      height: CHART_HEIGHT,
      layout: {
        fontSize: CHART_SCALE_FONT_SIZE,
      },
      localization: {
        locale: "ko-KR",
        timeFormatter: (t: Time) =>
          formatAxisTime(t, intradayModeRef.current),
      },
      crosshair: {
        mode: intradayModeRef.current
          ? CrosshairMode.MagnetOHLC
          : CrosshairMode.Normal,
      },
      timeScale: {
        fixLeftEdge: false,
        fixRightEdge: false,
        lockVisibleTimeRangeOnResize: true,
        rightOffset: 0,
        timeVisible: intradayModeRef.current,
        secondsVisible: false,
      },
      ...CHART_INTERACTION_OPTIONS,
    });
  }, [candles, error, intradayMode, loading]);

  useLayoutEffect(() => {
    forceInitialZoomRef.current = true;
    appliedFitKeyRef.current = null;
  }, [viewResetKey]);

  useLayoutEffect(() => {
    const chart = chartRef.current;

    if (error || (!loading && candles.length === 0)) {
      appliedFitKeyRef.current = null;

      return;
    }

    if (!chart || loading || error || candles.length === 0) {
      return;
    }

    const isPrependLoad =
      appliedFitKeyRef.current === viewResetKey &&
      prevCandlesLengthRef.current > 0 &&
      candles.length > prevCandlesLengthRef.current;

    if (isPrependLoad) {
      return;
    }

    const shouldApplyZoom =
      forceInitialZoomRef.current || appliedFitKeyRef.current !== viewResetKey;

    if (!shouldApplyZoom) {
      return;
    }

    forceInitialZoomRef.current = false;
    appliedFitKeyRef.current = viewResetKey;
    applyInitialZoom(chart, zoomProfileRef.current);
  }, [candles.length, error, loading, viewResetKey, zoomProfile]);

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
