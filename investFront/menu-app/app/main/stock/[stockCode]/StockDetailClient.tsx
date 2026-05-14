"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import StockCandleChart, {
  type ChartCandle,
} from "@/app/components/stock/StockCandleChart";
import { API_BASE_URL } from "@/lib/api-base";
import styles from "./stockCode.module.css";

import {
  createOrder,
  type OrderKind,
  type OrderType,
} from "@/lib/order";

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

type StaticProfileResponse = {
  stockCode?: string | null;
  stockName?: string | null;
  marketType?: string | null;
  sector?: string | null;
  listedDate?: string | null;
  status?: string | null;
  corpCode?: string | null;
  coName?: string | null;
  issuedStock?: string | null;
  declinedStock?: string | null;
  treasuryStock?: string | null;
  outstandingShares?: string | null;
  shareholdingRatio?: string | null;
  ownershipPercentage?: string | null;
};

type StockDetailResponse = {
  price: PriceResponse;
  profile: StaticProfileResponse | null;
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
const STOCK_NEWS_PAGE_SIZE = 5;

const tabs: Array<{ key: TabKey; label: string }> = [
  { key: "chart", label: "차트" },
  { key: "orderbook", label: "호가" },
  { key: "summary", label: "종목정보" },
  { key: "news", label: "뉴스" },
];

const chartPeriods: ChartPeriodLabel[] = ["일", "주", "월", "년"];

type NewsLoadPhase = "idle" | "loading" | "done";

export default function StockDetailClient({ stockCode }: { stockCode: string }) {
  const [activeTab, setActiveTab] = useState<TabKey>("chart");
  const [price, setPrice] = useState<PriceResponse | null>(null);
  const [orderbook, setOrderbook] = useState<OrderbookResponse | null>(null);
  const [profile, setProfile] = useState<StaticProfileResponse | null>(null);
  const [news, setNews] = useState<NewsResponse[]>([]);
  const [detailLoading, setDetailLoading] = useState(true);
  const [orderbookLoading, setOrderbookLoading] = useState(true);
  const [newsPhase, setNewsPhase] = useState<NewsLoadPhase>("idle");
  const [error, setError] = useState<string | null>(null);

  const snapshotSessionRef = useRef(0);
  const newsFetchInFlightRef = useRef(false);
  const newsDoneSessionRef = useRef<number | null>(null);

  const [orderKind, setOrderKind] = useState<OrderKind>("BUY");
  const [orderType, setOrderType] = useState<OrderType>("LIMIT");
  const [quantity, setQuantity] = useState("");
  const [orderLoading, setOrderLoading] = useState(false);
  const [orderMessage, setOrderMessage] = useState<string | null>(null);

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

  const runNewsFetch = useCallback(
    async (session: number) => {
      if (session !== snapshotSessionRef.current) {
        return;
      }

      if (newsFetchInFlightRef.current) {
        return;
      }

      if (newsDoneSessionRef.current === session) {
        return;
      }

      newsFetchInFlightRef.current = true;
      setNewsPhase("loading");

      try {
        const newsData = await fetchJson<NewsResponse[]>(
          `/api/public/news/stock/${stockCode}?size=${STOCK_NEWS_PAGE_SIZE}`
        );

        if (session !== snapshotSessionRef.current) {
          return;
        }

        setNews(Array.isArray(newsData) ? newsData : []);
        newsDoneSessionRef.current = session;
      } catch {
        if (session !== snapshotSessionRef.current) {
          return;
        }

        setNews([]);
        newsDoneSessionRef.current = session;
      } finally {
        newsFetchInFlightRef.current = false;

        if (session === snapshotSessionRef.current) {
          setNewsPhase("done");
        }
      }
    },
    [fetchJson, stockCode]
  );

  async function handleCreateOrder() {
    setOrderMessage(null);

    const currentPrice = Number(price?.currentPrice ?? 0);
    const orderQuantity = Number(quantity);

    if (!currentPrice || currentPrice <= 0) {
      setOrderMessage("현재가를 불러온 뒤 주문할 수 있습니다.");
      return;
    }

    if (!orderQuantity || orderQuantity <= 0) {
      setOrderMessage("주문 수량을 입력해주세요.");
      return;
    }

    try {
      setOrderLoading(true);

      const response = await createOrder({
        stockCode,
        orderKind,
        orderType,
        price: currentPrice,
        quantity: orderQuantity,
      });

      setOrderMessage(
        `${response.orderKind === "BUY" ? "매수" : "매도"} 주문이 완료되었습니다.`
      );

      setQuantity("");
    } catch (error) {
      console.error(error);
      setOrderMessage("주문 처리에 실패했습니다.");
    } finally {
      setOrderLoading(false);
    }
  }

  const loadSnapshot = useCallback(async () => {
    setError(null);

    snapshotSessionRef.current += 1;
    const session = snapshotSessionRef.current;

    newsDoneSessionRef.current = null;
    setNewsPhase("idle");
    setNews([]);
    setPrice(null);
    setProfile(null);
    setOrderbook(null);

    setDetailLoading(true);
    setOrderbookLoading(true);

    let detailOk = false;
    let orderbookOk = false;

    await Promise.all([
      (async () => {
        try {
          const detail = await fetchJson<StockDetailResponse>(
            `/api/stocks/${stockCode}/detail`
          );

          if (session !== snapshotSessionRef.current) {
            return;
          }

          setPrice(detail.price);
          setProfile(detail.profile);
          detailOk = true;
        } catch {
          if (session !== snapshotSessionRef.current) {
            return;
          }

          setPrice(null);
          setProfile(null);
        } finally {
          if (session === snapshotSessionRef.current) {
            setDetailLoading(false);
          }
        }
      })(),
      (async () => {
        try {
          const orderbookData = await fetchJson<OrderbookResponse>(
            `/api/stocks/${stockCode}/orderbook`
          );

          if (session !== snapshotSessionRef.current) {
            return;
          }

          setOrderbook(normalizeOrderbookResponse(orderbookData));
          orderbookOk = true;
        } catch {
          if (session !== snapshotSessionRef.current) {
            return;
          }

          setOrderbook(null);
        } finally {
          if (session === snapshotSessionRef.current) {
            setOrderbookLoading(false);
          }
        }
      })(),
    ]);

    if (session !== snapshotSessionRef.current) {
      return;
    }

    if (!detailOk && !orderbookOk) {
      setError("종목 정보를 불러오지 못했습니다.");
    }
  }, [fetchJson, stockCode]);

  const refreshQuote = useCallback(async () => {
    const [priceResult, orderbookResult] = await Promise.allSettled([
      fetchJson<PriceResponse>(`/api/stocks/${stockCode}/price`),
      fetchJson<OrderbookResponse>(`/api/stocks/${stockCode}/orderbook`),
    ]);
    if (priceResult.status === "fulfilled") {
      setPrice(priceResult.value);
    }

    if (orderbookResult.status === "fulfilled") {
      setOrderbook(normalizeOrderbookResponse(orderbookResult.value));
    }

    if (
      priceResult.status === "rejected" &&
      orderbookResult.status === "rejected"
    ) {
      setError("시세 갱신에 실패했습니다.");
    }
  }, [fetchJson, stockCode]);

  useEffect(() => {
    void loadSnapshot();
  }, [loadSnapshot]);

  useEffect(() => {
    const timer = window.setInterval(refreshQuote, QUOTE_REFRESH_INTERVAL_MS);

    return () => window.clearInterval(timer);
  }, [refreshQuote]);

  useEffect(() => {
    if (activeTab !== "news") {
      return;
    }

    if (detailLoading || orderbookLoading) {
      return;
    }

    void runNewsFetch(snapshotSessionRef.current);
  }, [activeTab, runNewsFetch, detailLoading, orderbookLoading]);

  const isUp = useMemo(() => {
    const rate = Number(price?.changeRate ?? 0);
    const change = Number(price?.changePrice ?? 0);

    return rate >= 0 && change >= 0;
  }, [price]);

  const displayName = price?.stockName || profile?.stockName || stockCode;
  const marketCap = useMemo(() => {
    const currentPrice = parseNumeric(price?.currentPrice);
    const outstandingShares = parseNumeric(profile?.outstandingShares ?? null);

    if (currentPrice === null || outstandingShares === null) {
      return null;
    }

    return currentPrice * outstandingShares;
  }, [price?.currentPrice, profile?.outstandingShares]);

  return (
    <main className={styles.page}>
      <section className={styles.hero}>
        <div className={styles.heroPrimary}>
          <div className={styles.heroNameRow}>
            <span className={styles.marketBadge}>
              {profile?.marketType || "VTS"}
            </span>
            <h1>{displayName}</h1>
            <span className={styles.heroCode}>{stockCode}</span>
          </div>
          <div className={styles.heroPriceRow}>
            <strong>{formatWon(price?.currentPrice)}</strong>
            <span className={styles.priceDot} aria-hidden>
              ·
            </span>
            <span className={styles.yesterdayLabel}>어제보다</span>
            <span className={isUp ? styles.up : styles.down}>
              {formatChange(price?.changePrice)} ({formatPercent(price?.changeRate)})
            </span>
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
            {activeTab === "chart" ? (
              <ChartShell stockCode={stockCode} fetchJson={fetchJson} />
            ) : null}
            {orderbookLoading && activeTab === "orderbook" ? (
              <EmptyState title="호가 정보를 불러오는 중입니다." />
            ) : null}
            {!orderbookLoading && activeTab === "orderbook" ? (
              <OrderbookPanel orderbook={orderbook} />
            ) : null}
            {detailLoading && activeTab === "summary" ? (
              <EmptyState title="종목 정보를 불러오는 중입니다." />
            ) : null}
            {!detailLoading && activeTab === "summary" ? (
              <SummaryPanel profile={profile} price={price} />
            ) : null}
            {activeTab === "news" && newsPhase !== "done" ? (
              <EmptyState title="뉴스를 불러오는 중입니다." />
            ) : null}
            {activeTab === "news" && newsPhase === "done" ? (
              <NewsPanel news={news} />
            ) : null}
          </div>
        </section>

        <aside className={styles.sidePanel}>
          <div className={styles.orderCard}>
            <div className={styles.orderTabs}>
              <button
                type="button"
                className={`${styles.orderTab} ${orderKind === "BUY" ? styles.buyTab : ""
                  }`}
                onClick={() => setOrderKind("BUY")}
              >
                구매
              </button>

              <button
                type="button"
                className={`${styles.orderTab} ${orderKind === "SELL" ? styles.sellTab : ""
                  }`}
                onClick={() => setOrderKind("SELL")}
              >
                판매
              </button>
            </div>

            <label>
              주문 유형
              <select
                value={orderType}
                onChange={(event) => setOrderType(event.target.value as OrderType)}
              >
                <option value="MARKET">시장가</option>
                {/* <option value="LIMIT">지정가</option> */}
              </select>
            </label>

            <label>
              {orderKind === "BUY" ? "구매 가격" : "판매 가격"}
              <input value={formatNumber(price?.currentPrice)} readOnly />
            </label>

            <label>
              수량
              <input
                value={quantity}
                onChange={(event) => setQuantity(event.target.value)}
                placeholder="수량 입력"
                inputMode="numeric"
              />
            </label>

            <button
              type="button"
              className={orderKind === "BUY" ? styles.buyButton : styles.sellButton}
              onClick={handleCreateOrder}
              disabled={orderLoading}
            >
              {orderLoading
                ? "처리 중..."
                : orderKind === "BUY"
                  ? "구매하기"
                  : "판매하기"}
            </button>

            {orderMessage ? (
              <p className={styles.orderMessage}>{orderMessage}</p>
            ) : null}
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
  profile,
  price,
}: {
  profile: StaticProfileResponse | null;
  price: PriceResponse | null;
}) {
  if (!profile && !price) {
    return <EmptyState title="종목 요약 정보가 없습니다." />;
  }

  return (
    <div className={styles.summaryGrid}>
      <Stat label="시장" value={profile?.marketType || "-"} />
      <Stat label="상장일" value={profile?.listedDate || "-"} />
      <Stat label="업종" value={profile?.sector || "-"} />
      <Stat label="회사명" value={profile?.coName || "-"} />
      <Stat label="공시 코드" value={profile?.corpCode || "-"} />
      <Stat label="상태" value={profile?.status || "-"} />
      <Stat
        label="발행주식수"
        value={formatNumber(
          profile?.issuedStock === null || profile?.issuedStock === undefined
            ? null
            : String(profile.issuedStock)
        )}
      />
      <Stat
        label="유통주식수"
        value={formatNumber(
          profile?.outstandingShares === null || profile?.outstandingShares === undefined
            ? null
            : String(profile.outstandingShares)
        )}
      />
      <Stat
        label="자기주식수"
        value={formatNumber(
          profile?.treasuryStock === null || profile?.treasuryStock === undefined
            ? null
            : String(profile.treasuryStock)
        )}
      />
      <Stat
        label="소액주주 지분율"
        value={formatPlainPercent(profile?.shareholdingRatio)}
      />
      <Stat
        label="소액주주 소유율"
        value={formatPlainPercent(profile?.ownershipPercentage)}
      />
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

function formatPlainPercent(value?: string | null) {
  const numeric = Number(String(value ?? "").replaceAll(",", ""));

  if (!Number.isFinite(numeric) || value === null || value === undefined || value === "") {
    return "-";
  }

  return `${numeric.toFixed(2)}%`;
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
