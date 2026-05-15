"use client";

import { useEffect, useMemo, useRef, useState } from "react";

import { API_BASE_URL, getPublicApiBase } from "@/lib/api-base";

import NewsArticleModal from "./NewsArticleModal";
import RelatedStockChips from "./RelatedStockChips";
import { formatRelativeTimeKo, shortenHost, thumbLetter } from "./newsFormat";
import styles from "./NewsPage.module.css";
import type { NewsItem } from "./newsTypes";

export type { NewsItem };

const PAGE_SIZE = 7;
const BATCH_MAX_CODES = 100;
const NEWS_MARKET_FULL_LIMIT = 100;

type BatchPriceMap = Record<string, string | null | undefined>;

function idleYield(signal: AbortSignal): Promise<void> {
  return new Promise((resolve, reject) => {
    if (signal.aborted) {
      reject(new DOMException("Aborted", "AbortError"));
      return;
    }
    const onAbort = () => reject(new DOMException("Aborted", "AbortError"));
    signal.addEventListener("abort", onAbort, { once: true });
    const finish = () => {
      signal.removeEventListener("abort", onAbort);
      resolve();
    };
    if (typeof requestIdleCallback !== "undefined") {
      requestIdleCallback(() => finish(), { timeout: 1500 });
    } else {
      window.setTimeout(finish, 16);
    }
  });
}

async function fetchChangeRatesViaBatchApi(
  codes: string[],
  signal: AbortSignal,
): Promise<Record<string, string | null>> {
  const unique = [...new Set(codes.map((c) => c.trim()).filter(Boolean))];
  const out: Record<string, string | null> = {};
  for (let i = 0; i < unique.length; i += BATCH_MAX_CODES) {
    const chunk = unique.slice(i, i + BATCH_MAX_CODES);
    const res = await fetch(`${API_BASE_URL}/api/stocks/prices/batch`, {
      method: "POST",
      credentials: "include",
      cache: "no-store",
      signal,
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ stockCodes: chunk }),
    });
    if (!res.ok) {
      continue;
    }
    const data = (await res.json()) as BatchPriceMap;
    if (data != null && typeof data === "object") {
      for (const k of Object.keys(data)) {
        const v = data[k];
        out[k] =
          v != null && String(v).trim() !== "" ? String(v) : null;
      }
    }
  }
  return out;
}

function collectRelatedStockCodes(rows: NewsItem[]): string[] {
  const set = new Set<string>();
  for (const it of rows) {
    const list = it.relatedStocks;
    if (!list?.length) continue;
    for (const s of list) {
      const c = s.stockCode?.trim();
      if (c) set.add(c);
    }
  }
  return [...set];
}

function mergeByArticleLink(existing: NewsItem[], incoming: NewsItem[]): NewsItem[] {
  const seen = new Set<string>();
  const out: NewsItem[] = [];
  for (const it of existing) {
    const k = (it.articleLink || "").trim();
    const id = k || `local:${out.length}:${it.title ?? ""}`;
    if (seen.has(id)) continue;
    seen.add(id);
    out.push(it);
  }
  for (const it of incoming) {
    const k = (it.articleLink || "").trim();
    if (!k) {
      out.push(it);
      continue;
    }
    if (seen.has(k)) continue;
    seen.add(k);
    out.push(it);
  }
  return out;
}

// 상위 카테고리(고정). 버튼은 상시 표기, 데이터는 카테고리 매핑 후 필터링
const MAIN_CATEGORIES: Array<"" | "반도체" | "증시" | "경제" | "금리" | "환율" | "유가"> = [
  "",
  "반도체",
  "증시",
  "경제",
  "금리",
  "환율",
  "유가",
];

type PaginationEntry = number | "ellipsis";

/**
 *  1 2 3 4 5 ... 10  — 앞쪽 5페이지, 생략, 마지막 페이지.
 * 중간·끝 구간은 현재 페이지에 맞춰 조정.
 */
function getPaginationEntries(current: number, totalPages: number): PaginationEntry[] {
  if (totalPages <= 7) {
    return Array.from({ length: totalPages }, (_, i) => i + 1);
  }

  if (current <= 5) {
    return [1, 2, 3, 4, 5, "ellipsis", totalPages];
  }

  if (current >= totalPages - 4) {
    return [
      1,
      "ellipsis",
      totalPages - 4,
      totalPages - 3,
      totalPages - 2,
      totalPages - 1,
      totalPages,
    ];
  }

  return [1, "ellipsis", current - 1, current, current + 1, "ellipsis", totalPages];
}

type Props = {
  ok: boolean;
  items: NewsItem[];
};

function toTopCategory(item: NewsItem): "" | "반도체" | "증시" | "경제" | "금리" | "환율" | "유가" {
  const label = item.primaryLabel?.trim() || "";
  const kind = item.keywordKind || null;
  if (!label) return "";

  const l = label.toLowerCase();

  const oil = ["유가", "원유", "wti", "브렌트", "brent"];
  if (oil.some((k) => l.includes(k.toLowerCase()))) return "유가";

  const fx = ["환율", "달러", "원/달러", "엔화", "위안"];
  if (fx.some((k) => l.includes(k.toLowerCase()))) return "환율";

  const rate = ["금리", "기준금리", "fomc", "연준", "fed", "국채", "채권", "국고채"];
  if (rate.some((k) => l.includes(k.toLowerCase()))) return "금리";

  const market = ["증시", "코스피", "코스닥", "나스닥", "s&p", "다우"];
  if (market.some((k) => l.includes(k.toLowerCase()))) return "증시";

  const semi = ["반도체", "sk하이닉스", "삼성전자", "하이닉스"];
  if (semi.some((k) => l.includes(k.toLowerCase()))) return "반도체";

  if (kind === "MACRO" || l.includes("경제")) return "경제";
  if (kind === "STOCK" || kind === "SECTOR") return "증시";
  return "경제";
}

export default function NewsFeedClient({ ok, items }: Props) {
  const [page, setPage] = useState(1);
  const listTopRef = useRef<HTMLDivElement>(null);
  const [selectedCategory, setSelectedCategory] = useState<
    "" | "반도체" | "증시" | "경제" | "금리" | "환율" | "유가"
  >("");
  const [loadOk, setLoadOk] = useState(ok);
  const [dataAll, setDataAll] = useState<NewsItem[]>(items);
  const [selected, setSelected] = useState<NewsItem | null>(null);
  const [liveChangeRateByCode, setLiveChangeRateByCode] = useState<
    Record<string, string | null>
  >({});
  const [loadingMore, setLoadingMore] = useState(false);
  const [loadMoreFailed, setLoadMoreFailed] = useState(false);
  const [loadMoreExhausted, setLoadMoreExhausted] = useState(false);

  const liveRatesRef = useRef<Record<string, string | null>>({});
  liveRatesRef.current = liveChangeRateByCode;

  // server component에서 내려준 초기 데이터/상태가 바뀌면 동기화
  useEffect(() => {
    setLoadOk(ok);
    setDataAll(items);
    setPage(1);
    setSelectedCategory("");
    setSelected(null);
    setLiveChangeRateByCode({});
    setLoadMoreFailed(false);
    setLoadMoreExhausted(false);
    setLoadingMore(false);
  }, [ok, items]);

  const data = useMemo(() => {
    if (!selectedCategory) return dataAll;
    return dataAll.filter((it) => toTopCategory(it) === selectedCategory);
  }, [dataAll, selectedCategory]);

  useEffect(() => {
    const fullList = collectRelatedStockCodes(data);
    if (fullList.length === 0) {
      return;
    }

    const snap = liveRatesRef.current;
    const start = (page - 1) * PAGE_SIZE;
    const shownSlice = data.slice(start, start + PAGE_SIZE);
    const visibleList = collectRelatedStockCodes(shownSlice);
    const visibleSet = new Set(visibleList);
    const restList = fullList.filter((c) => !visibleSet.has(c));

    const needVisible = visibleList.filter((c) => !(c in snap));
    const needRest = restList.filter((c) => !(c in snap));

    const ac = new AbortController();
    let cancelled = false;

    void (async () => {
      try {
        if (needVisible.length > 0) {
          const mapVis = await fetchChangeRatesViaBatchApi(needVisible, ac.signal);
          if (!cancelled) {
            setLiveChangeRateByCode((prev) => ({ ...prev, ...mapVis }));
          }
        }
        if (needRest.length > 0) {
          await idleYield(ac.signal);
          if (cancelled) return;
          const mapRest = await fetchChangeRatesViaBatchApi(needRest, ac.signal);
          if (!cancelled) {
            setLiveChangeRateByCode((prev) => ({ ...prev, ...mapRest }));
          }
        }
      } catch {
      }
    })();

    return () => {
      cancelled = true;
      ac.abort();
    };
  }, [data, page]);

  const total = data.length;
  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE));

  useEffect(() => {
    if (page > totalPages) {
      setPage(totalPages);
    }
  }, [page, totalPages]);

  const shown = useMemo(() => {
    const start = (page - 1) * PAGE_SIZE;
    return data.slice(start, start + PAGE_SIZE);
  }, [data, page]);

  const paginationEntries = useMemo(
    () => getPaginationEntries(page, totalPages),
    [page, totalPages]
  );

  const goToPage = (p: number) => {
    const next = Math.min(Math.max(1, p), totalPages);
    setPage(next);
    listTopRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
  };

  const onSelectCategory = (cat: "" | "반도체" | "증시" | "경제" | "금리" | "환율" | "유가") => {
    setSelectedCategory(cat);
    setPage(1);
  };

  const showLoadMore =
    loadOk &&
    !loadMoreFailed &&
    !loadMoreExhausted &&
    dataAll.length < NEWS_MARKET_FULL_LIMIT;

  const onLoadMore = async () => {
    if (loadingMore || !showLoadMore) return;
    setLoadingMore(true);
    try {
      const base = getPublicApiBase();
      const res = await fetch(
        `${base}/api/public/news/market?size=${NEWS_MARKET_FULL_LIMIT}`,
        {
          method: "GET",
          credentials: "include",
          cache: "no-store",
        },
      );
      if (!res.ok) {
        setLoadMoreFailed(true);
        return;
      }
      const data = (await res.json()) as NewsItem[];
      if (!Array.isArray(data)) {
        setLoadMoreFailed(true);
        return;
      }
      const merged = mergeByArticleLink(dataAll, data);
      if (merged.length === dataAll.length) {
        setLoadMoreExhausted(true);
      }
      setDataAll(merged);
      setPage(1);
      setSelectedCategory("");
    } catch {
      setLoadMoreFailed(true);
    } finally {
      setLoadingMore(false);
    }
  };

  return (
    <>
      <div ref={listTopRef} className={styles.listAnchor} aria-hidden />

      <div className={styles.tagBar} aria-label="주요 키워드">
        {MAIN_CATEGORIES.map((cat) => (
          <button
            key={cat || "ALL"}
            type="button"
            className={selectedCategory === cat ? styles.tagBtnActive : styles.tagBtn}
            onClick={() => onSelectCategory(cat)}
          >
            {cat || "전체"}
          </button>
        ))}
      </div>

      {!loadOk ? (
        <div className={styles.error}>
          뉴스를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.
        </div>
      ) : total === 0 ? (
        <div className={styles.empty}>
          아직 표시할 최신 뉴스가 없습니다. 상단의 “전체”로 돌아가거나 잠시 후 다시 확인해 주세요.
        </div>
      ) : (
        <div className={styles.list} role="list">
          {shown.map((item, idx) => {
            const globalIdx = (page - 1) * PAGE_SIZE + idx;
            const label = item.primaryLabel?.trim() || "";
            const kind = item.keywordKind || null;
            const badgeClass =
              kind === "STOCK"
                ? styles.badgeStock
                : kind === "SECTOR"
                  ? styles.badgeSector
                  : kind === "MACRO"
                    ? styles.badgeMacro
                    : kind === "ISSUE"
                      ? styles.badgeIssue
                      : styles.badgeNeutral;
            return (
              <button
                key={`${item.articleLink || "n"}-${globalIdx}`}
                type="button"
                className={styles.card}
                role="listitem"
                onClick={() => setSelected(item)}
              >
                <div className={styles.thumb} aria-hidden>
                  {label ? (
                    <span className={`${styles.thumbBadge} ${badgeClass}`}>
                      {label}
                    </span>
                  ) : (
                    thumbLetter(item.title)
                  )}
                </div>
                <div className={styles.body}>
                  <h3 className={styles.title}>{item.title}</h3>
                  <div className={styles.metaRow}>
                    {item.relatedStocks && item.relatedStocks.length > 0 ? (
                      <div className={styles.metaChips}>
                        <RelatedStockChips
                          items={item.relatedStocks}
                          liveChangeRateByCode={liveChangeRateByCode}
                          size="compact"
                          variant="inline"
                        />
                      </div>
                    ) : null}
                    <div className={styles.metaInfo}>
                      <span className={styles.publisher}>
                        {shortenHost(item.publisher)}
                      </span>
                      <span className={styles.dot}>·</span>
                      <span className={styles.time}>
                        {formatRelativeTimeKo(item.publishedAt)}
                      </span>
                    </div>
                  </div>
                </div>
              </button>
            );
          })}
        </div>
      )}

      {showLoadMore ? (
        <div className={styles.loadMoreRow}>
          <button
            type="button"
            className={styles.loadMoreBtn}
            onClick={() => void onLoadMore()}
            disabled={loadingMore}
          >
            {loadingMore ? "불러오는 중…" : "뉴스 더 불러오기"}
          </button>
        </div>
      ) : null}

      {totalPages > 1 ? (
        <nav
          className={styles.pagination}
          aria-label="뉴스 페이지"
        >
          <button
            type="button"
            className={styles.pagerArrow}
            onClick={() => goToPage(page - 1)}
            disabled={page <= 1}
            aria-label="이전 페이지"
          >
            &lt;
          </button>

          <div className={styles.pagerPages}>
            {paginationEntries.map((entry, idx) =>
              entry === "ellipsis" ? (
                <span
                  key={`e-${idx}`}
                  className={styles.pagerEllipsis}
                  aria-hidden
                >
                  ...
                </span>
              ) : (
                <button
                  key={entry}
                  type="button"
                  className={
                    entry === page ? styles.pagerNumActive : styles.pagerNum
                  }
                  onClick={() => goToPage(entry)}
                  aria-current={entry === page ? "page" : undefined}
                  aria-label={`${entry}페이지`}
                >
                  {entry}
                </button>
              )
            )}
          </div>

          <button
            type="button"
            className={styles.pagerArrow}
            onClick={() => goToPage(page + 1)}
            disabled={page >= totalPages}
            aria-label="다음 페이지"
          >
            &gt;
          </button>
        </nav>
      ) : null}

      <NewsArticleModal
        item={selected}
        liveChangeRateByCode={liveChangeRateByCode}
        onClose={() => setSelected(null)}
      />
    </>
  );
}
