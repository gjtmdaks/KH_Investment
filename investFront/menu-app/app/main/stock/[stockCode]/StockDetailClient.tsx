"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import StockCandleChart, {
  type ChartCandle,
} from "@/app/components/stock/StockCandleChart";
import { API_BASE_URL } from "@/lib/api-base";
import styles from "./stockCode.module.css";

type PriceResponse = {
  stockCode: string;
  stockName?: string | null;
  currentPrice?: string | null;
  changePrice?: string | null;
  changeRate?: string | null;
  volume?: string | null;
  tradingValue?: string | null;
  openPrice?: string | null;
  highPrice?: string | null;
  lowPrice?: string | null;
};

type OrderbookLevel = {
  level: number;
  price?: string | null;
  quantity?: string | null;
  quantityChange?: string | null;
};

type OrderbookResponse = {
  stockCode: string;
  asks: OrderbookLevel[];
  bids: OrderbookLevel[];
  totalAskQuantity?: string | null;
  totalBidQuantity?: string | null;
  expectedPrice?: string | null;
  expectedQuantity?: string | null;
};

type SummaryResponse = {
  stockCode: string;
  stockName?: string | null;
  marketId?: string | null;
  stockGroup?: string | null;
  stockKind?: string | null;
  listedShares?: string | null;
  capital?: string | null;
  parValue?: string | null;
  listedDate?: string | null;
  fiscalMonth?: string | null;
  isKospi200?: string | null;
  standardCode?: string | null;
  listingAbolitionDate?: string | null;
};

type NewsResponse = {
  newsInfoId: number;
  title: string;
  description?: string | null;
  publisher?: string | null;
  primaryLabel?: string | null;
  articleLink?: string | null;
  publishedAt?: string | null;
};

type ChartPeriodLabel = "일" | "주" | "월" | "년";

type TabKey = "chart" | "orderbook" | "summary" | "news";

const QUOTE_REFRESH_INTERVAL_MS = 30_000;

const tabs: Array<{ key: TabKey; label: string }> = [
  { key: "chart", label: "차트" },
  { key: "orderbook", label: "호가" },
  { key: "summary", label: "종목정보" },
  { key: "news", label: "뉴스" },
];

const chartPeriods: ChartPeriodLabel[] = ["일", "주", "월", "년"];

export default function StockDetailClient({ stockCode }: { stockCode: string }) {
  const [activeTab, setActiveTab] = useState<TabKey>("chart");
  const [price, setPrice] = useState<PriceResponse | null>(null);
  const [orderbook, setOrderbook] = useState<OrderbookResponse | null>(null);
  const [summary, setSummary] = useState<SummaryResponse | null>(null);
  const [news, setNews] = useState<NewsResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchJson = useCallback(async <T,>(path: string): Promise<T> => {
    const response = await fetch(`${API_BASE_URL}${path}`, {
      credentials: "include",
      cache: "no-store",
    });

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }

    return response.json() as Promise<T>;
  }, []);

  const loadSnapshot = useCallback(async () => {
    setError(null);

    try {
      const detail = await fetchJson<{ price: PriceResponse; summary: SummaryResponse }>(
        `/api/stocks/${stockCode}/detail`
      );
      const orderbookData = await fetchJson<OrderbookResponse>(
        `/api/stocks/${stockCode}/orderbook`
      );
      const newsData = await fetchJson<NewsResponse[]>(
        `/api/public/news/stock/${stockCode}?size=8`
      );

      setPrice(detail.price);
      setSummary(detail.summary);
      setOrderbook(normalizeOrderbookResponse(orderbookData));
      setNews(Array.isArray(newsData) ? newsData : []);
    } catch {
      setError("종목 정보를 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  }, [fetchJson, stockCode]);

  const refreshQuote = useCallback(async () => {
    try {
      const priceData = await fetchJson<PriceResponse>(
        `/api/stocks/${stockCode}/price`
      );
      const orderbookData = await fetchJson<OrderbookResponse>(
        `/api/stocks/${stockCode}/orderbook`
      );

      setPrice(priceData);
      setOrderbook(normalizeOrderbookResponse(orderbookData));
    } catch {
      setError("시세 갱신에 실패했습니다.");
    }
  }, [fetchJson, stockCode]);

  useEffect(() => {
    const timer = window.setTimeout(loadSnapshot, 0);

    return () => window.clearTimeout(timer);
  }, [loadSnapshot]);

  useEffect(() => {
    const timer = window.setInterval(refreshQuote, QUOTE_REFRESH_INTERVAL_MS);

    return () => window.clearInterval(timer);
  }, [refreshQuote]);

  const isUp = useMemo(() => {
    const rate = Number(price?.changeRate ?? 0);
    const change = Number(price?.changePrice ?? 0);

    return rate >= 0 && change >= 0;
  }, [price]);

  const displayName = price?.stockName || summary?.stockName || stockCode;
  const marketCap = useMemo(() => {
    const currentPrice = parseNumeric(price?.currentPrice);
    const listedShares = parseNumeric(summary?.listedShares);

    if (currentPrice === null || listedShares === null) {
      return null;
    }

    return currentPrice * listedShares;
  }, [price?.currentPrice, summary?.listedShares]);

  return (
    <main className={styles.page}>
      <section className={styles.hero}>
        <div>
          <div className={styles.stockMeta}>
            <span className={styles.marketBadge}>
              {summary?.marketId || "KIS"}
            </span>
            <span>{stockCode}</span>
          </div>
          <div className={styles.heroTitleRow}>
            <h1>{displayName}</h1>
            <div className={styles.priceLine}>
              <strong>{formatWon(price?.currentPrice)}</strong>
              <span className={isUp ? styles.up : styles.down}>
                {formatChange(price?.changePrice)} ({formatPercent(price?.changeRate)})
              </span>
            </div>
          </div>
        </div>

        <div className={styles.heroStats}>
          <Stat label="거래량(주)" value={formatNumber(price?.volume)} />
          <Stat label="거래대금" value={formatKoreanLargeWon(price?.tradingValue)} />
          <Stat label="시가총액" value={formatKoreanLargeWon(marketCap)} />
          <Stat label="고가" value={formatWon(price?.highPrice)} />
          <Stat label="저가" value={formatWon(price?.lowPrice)} />
        </div>
      </section>

      {error ? <div className={styles.error}>{error}</div> : null}

      <div className={styles.layout}>
        <section className={styles.mainPanel}>
          <nav className={styles.tabs}>
            {tabs.map((tab) => (
              <button
                key={tab.key}
                type="button"
                className={activeTab === tab.key ? styles.activeTab : ""}
                onClick={() => setActiveTab(tab.key)}
              >
                {tab.label}
              </button>
            ))}
          </nav>

          <div className={styles.panelBody}>
            {loading && activeTab !== "chart" ? (
              <EmptyState title="종목 데이터를 불러오는 중입니다." />
            ) : null}
            {activeTab === "chart" ? (
              <ChartShell stockCode={stockCode} fetchJson={fetchJson} />
            ) : null}
            {!loading && activeTab === "orderbook" ? (
              <OrderbookPanel orderbook={orderbook} />
            ) : null}
            {!loading && activeTab === "summary" ? (
              <SummaryPanel summary={summary} price={price} />
            ) : null}
            {!loading && activeTab === "news" ? <NewsPanel news={news} /> : null}
          </div>
        </section>

        <aside className={styles.sidePanel}>
          <div className={styles.orderCard}>
            <div className={styles.orderTabs}>
              <button type="button" className={styles.buyTab}>
                구매
              </button>
              <button type="button">판매</button>
              <button type="button">대기</button>
            </div>
            <label>
              주문 유형
              <select defaultValue="LIMIT">
                <option value="LIMIT">지정가</option>
                <option value="MARKET">시장가</option>
              </select>
            </label>
            <label>
              구매 가격
              <input value={formatNumber(price?.currentPrice)} readOnly />
            </label>
            <label>
              수량
              <input placeholder="수량 입력" />
            </label>
            <button type="button" className={styles.buyButton}>
              구매 기능 없음
            </button>
          </div>

          <div className={styles.snapshotCard}>
            <h3>내 주식 요약</h3>
            <p>기능 X.</p>
          </div>
        </aside>
      </div>
    </main>
  );
}

function ChartShell({
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

function OrderbookPanel({ orderbook }: { orderbook: OrderbookResponse | null }) {
  if (!orderbook) {
    return <EmptyState title="호가 정보를 불러오지 못했습니다." />;
  }

  const asks = normalizeOrderbookLevels(orderbook.asks);
  const bids = normalizeOrderbookLevels(orderbook.bids);

  if (asks.length === 0 && bids.length === 0) {
    return <EmptyState title="호가 정보를 불러오지 못했습니다." />;
  }

  return (
    <div className={styles.orderbookGrid}>
      <div>
        <h3>매도 호가</h3>
        {[...asks].reverse().map((level) => (
          <OrderbookRow key={`ask-${level.level}`} level={level} side="ask" />
        ))}
        <div className={styles.totalRow}>
          총 매도잔량 {formatNumber(orderbook.totalAskQuantity)}
        </div>
      </div>

      <div>
        <h3>매수 호가</h3>
        {bids.map((level) => (
          <OrderbookRow key={`bid-${level.level}`} level={level} side="bid" />
        ))}
        <div className={styles.totalRow}>
          총 매수잔량 {formatNumber(orderbook.totalBidQuantity)}
        </div>
      </div>
    </div>
  );
}

function OrderbookRow({
  level,
  side,
}: {
  level: OrderbookLevel;
  side: "ask" | "bid";
}) {
  return (
    <div className={`${styles.orderbookRow} ${styles[side]}`}>
      <span>{formatWon(level.price)}</span>
      <span>{formatNumber(level.quantity)}</span>
    </div>
  );
}

function SummaryPanel({
  summary,
  price,
}: {
  summary: SummaryResponse | null;
  price: PriceResponse | null;
}) {
  if (!summary && !price) {
    return <EmptyState title="종목 요약 정보가 없습니다." />;
  }

  return (
    <div className={styles.summaryGrid}>
      <Stat label="시장" value={summary?.marketId || "-"} />
      <Stat label="표준코드" value={summary?.standardCode || "-"} />
      <Stat label="상장주식수" value={formatNumber(summary?.listedShares)} />
      <Stat label="자본금" value={formatWon(summary?.capital)} />
      <Stat label="액면가" value={formatWon(summary?.parValue)} />
      <Stat label="결산월" value={summary?.fiscalMonth || "-"} />
      <Stat label="시가" value={formatWon(price?.openPrice)} />
      <Stat label="고가 / 저가" value={`${formatWon(price?.highPrice)} / ${formatWon(price?.lowPrice)}`} />
    </div>
  );
}

function NewsPanel({ news }: { news: NewsResponse[] }) {
  if (news.length === 0) {
    return <EmptyState title="관련 뉴스가 없습니다." />;
  }

  return (
    <div className={styles.newsList}>
      {news.map((item) => (
        <a
          key={item.newsInfoId}
          href={item.articleLink || "#"}
          target="_blank"
          rel="noreferrer"
          className={styles.newsItem}
        >
          <span>{item.publisher || "뉴스"}</span>
          <strong>{stripHtml(item.title)}</strong>
          <p>{stripHtml(item.description || "")}</p>
        </a>
      ))}
    </div>
  );
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className={styles.stat}>
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function EmptyState({ title }: { title: string }) {
  return <div className={styles.empty}>{title}</div>;
}

function normalizeOrderbookResponse(data: unknown): OrderbookResponse | null {
  if (!data || typeof data !== "object") {
    return null;
  }

  const raw = data as Partial<OrderbookResponse>;

  if (!raw.stockCode) {
    return null;
  }

  return {
    stockCode: raw.stockCode,
    asks: normalizeOrderbookLevels(raw.asks),
    bids: normalizeOrderbookLevels(raw.bids),
    totalAskQuantity: raw.totalAskQuantity ?? null,
    totalBidQuantity: raw.totalBidQuantity ?? null,
    expectedPrice: raw.expectedPrice ?? null,
    expectedQuantity: raw.expectedQuantity ?? null,
  };
}

function normalizeOrderbookLevels(value: unknown): OrderbookLevel[] {
  if (!Array.isArray(value)) {
    return [];
  }

  return value
    .filter((item): item is OrderbookLevel => {
      return Boolean(item) && typeof item === "object" && "level" in item;
    })
    .map((item) => ({
      level: Number(item.level),
      price: item.price ?? null,
      quantity: item.quantity ?? null,
      quantityChange: item.quantityChange ?? null,
    }))
    .filter((item) => Number.isFinite(item.level));
}

function formatNumber(value?: string | null) {
  const numeric = parseNumeric(value);

  if (numeric === null || value === null || value === undefined || value === "") {
    return "-";
  }

  return numeric.toLocaleString("ko-KR");
}

function parseNumeric(value?: string | null) {
  const numeric = Number(String(value ?? "").replaceAll(",", ""));

  return Number.isFinite(numeric) ? numeric : null;
}

function formatKoreanLargeWon(value?: string | number | null) {
  const numeric = typeof value === "number" ? value : parseNumeric(value);

  if (numeric === null) {
    return "-";
  }

  const ONE_EOK = 100_000_000;
  const ONE_JO = 1_000_000_000_000;

  if (numeric >= ONE_JO) {
    return `${formatScaledAmount(numeric / ONE_JO, 1)}조원`;
  }

  if (numeric >= ONE_EOK) {
    const decimals = numeric >= ONE_EOK * 100 ? 0 : 1;
    return `${formatScaledAmount(numeric / ONE_EOK, decimals)}억원`;
  }

  if (numeric >= 10_000) {
    return `${Math.round(numeric / 10_000).toLocaleString("ko-KR")}만원`;
  }

  return `${numeric.toLocaleString("ko-KR")}원`;
}

function formatScaledAmount(value: number, decimals: number) {
  return value.toLocaleString("ko-KR", {
    minimumFractionDigits: 0,
    maximumFractionDigits: decimals,
  });
}

function formatWon(value?: string | null) {
  const formatted = formatNumber(value);

  return formatted === "-" ? formatted : `${formatted}원`;
}

function formatPercent(value?: string | null) {
  const numeric = Number(String(value ?? "").replaceAll(",", ""));

  if (!Number.isFinite(numeric) || value === null || value === undefined || value === "") {
    return "-";
  }

  return `${numeric > 0 ? "+" : ""}${numeric.toFixed(2)}%`;
}

function formatChange(value?: string | null) {
  const numeric = Number(String(value ?? "").replaceAll(",", ""));

  if (!Number.isFinite(numeric) || value === null || value === undefined || value === "") {
    return "-";
  }

  return `${numeric > 0 ? "+" : ""}${numeric.toLocaleString("ko-KR")}원`;
}

function stripHtml(value: string) {
  return value.replace(/<[^>]*>/g, "").replaceAll("&quot;", "\"").replaceAll("&amp;", "&");
}

function getApiPeriod(period: ChartPeriodLabel) {
  if (period === "일") {
    return "D";
  }

  if (period === "주") {
    return "W";
  }

  return "M";
}

function getChartDateRange(period: ChartPeriodLabel) {
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

function normalizeCandlePayload(payload: unknown): ChartCandle[] {
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

function aggregateYearlyCandles(candles: ChartCandle[]) {
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
