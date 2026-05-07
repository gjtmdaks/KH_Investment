"use client";

import Link from "next/link";
import { useEffect, useMemo, useRef, useState } from "react";
import styles from "./NewsPage.module.css";

export type NewsItem = {
  newsInfoId: number | null;
  title: string;
  description: string;
  publisher: string;
  articleLink: string;
  publishedAt: string;
};

const PAGE_SIZE = 5;

type PaginationEntry = number | "ellipsis";

function formatRelativeTimeKo(iso: string): string {
  if (!iso) return "";
  const t = Date.parse(iso);
  if (Number.isNaN(t)) return "";
  const diff = Date.now() - t;
  if (diff < 60_000) return "방금 전";
  const mins = Math.floor(diff / 60_000);
  if (mins < 60) return `${mins}분 전`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}시간 전`;
  const days = Math.floor(hrs / 24);
  if (days < 7) return `${days}일 전`;
  return new Date(t).toLocaleDateString("ko-KR", {
    month: "short",
    day: "numeric",
  });
}

function thumbLetter(title: string): string {
  const t = title?.trim();
  if (!t) return "·";
  const ch = t[0];
  if (/[a-zA-Z]/.test(ch)) return ch.toUpperCase();
  return ch;
}

function shortenHost(publisher: string): string {
  if (!publisher || publisher === "-") return "출처 미상";
  return publisher.replace(/^www\./, "");
}

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

export default function NewsFeedClient({ ok, items }: Props) {
  const [page, setPage] = useState(1);
  const listTopRef = useRef<HTMLDivElement>(null);

  const total = items.length;
  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE));

  useEffect(() => {
    if (page > totalPages) {
      setPage(totalPages);
    }
  }, [page, totalPages]);

  const shown = useMemo(() => {
    const start = (page - 1) * PAGE_SIZE;
    return items.slice(start, start + PAGE_SIZE);
  }, [items, page]);

  const paginationEntries = useMemo(
    () => getPaginationEntries(page, totalPages),
    [page, totalPages]
  );

  const goToPage = (p: number) => {
    const next = Math.min(Math.max(1, p), totalPages);
    setPage(next);
    listTopRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
  };

  if (!ok) {
    return (
      <div className={styles.error}>
        뉴스를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.
      </div>
    );
  }

  if (total === 0) {
    return (
      <div className={styles.empty}>
        표시할 뉴스가 없습니다. API 키·네트워크 또는 잠시 후 다시 확인해
        주세요.
      </div>
    );
  }

  return (
    <>
      <div ref={listTopRef} className={styles.listAnchor} aria-hidden />
      <div className={styles.list} role="list">
        {shown.map((item, idx) => {
          const globalIdx = (page - 1) * PAGE_SIZE + idx;
          return (
            <Link
              key={`${item.articleLink || "n"}-${globalIdx}`}
              href={item.articleLink || "#"}
              target="_blank"
              rel="noopener noreferrer"
              className={styles.card}
              role="listitem"
            >
              <div className={styles.thumb} aria-hidden>
                {thumbLetter(item.title)}
              </div>
              <div className={styles.body}>
                <h3 className={styles.title}>{item.title}</h3>
                {item.description ? (
                  <p className={styles.desc}>{item.description}</p>
                ) : null}
                <div className={styles.meta}>
                  <span className={styles.publisher}>
                    {shortenHost(item.publisher)}
                  </span>
                  <span className={styles.dot}>·</span>
                  <span className={styles.time}>
                    {formatRelativeTimeKo(item.publishedAt)}
                  </span>
                </div>
              </div>
            </Link>
          );
        })}
      </div>

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
    </>
  );
}
