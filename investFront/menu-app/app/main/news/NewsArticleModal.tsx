"use client";

import { useEffect, useId, useRef } from "react";
import type { NewsItem } from "./newsTypes";
import { ensurePreviewTrailingEllipsis, formatRelativeTimeKo, shortenHost, thumbLetter } from "./newsFormat";
import RelatedStockChips from "./RelatedStockChips";
import styles from "./NewsArticleModal.module.css";

type Props = {
  item: NewsItem | null;
  onClose: () => void;
};

function badgeClassFor(
  kind: NewsItem["keywordKind"]
): keyof Pick<
  typeof styles,
  | "badgeStock"
  | "badgeSector"
  | "badgeMacro"
  | "badgeIssue"
  | "badgeNeutral"
> {
  if (kind === "STOCK") return "badgeStock";
  if (kind === "SECTOR") return "badgeSector";
  if (kind === "MACRO") return "badgeMacro";
  if (kind === "ISSUE") return "badgeIssue";
  return "badgeNeutral";
}

export default function NewsArticleModal({ item, onClose }: Props) {
  const titleId = useId();
  const closeRef = useRef<HTMLButtonElement>(null);
  const open = Boolean(item);

  useEffect(() => {
    if (!open) return;
    const prevOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    closeRef.current?.focus();
    return () => {
      document.body.style.overflow = prevOverflow;
    };
  }, [open]);

  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [open, onClose]);

  if (!item) return null;

  const label = item.primaryLabel?.trim() || "";
  const badgeKey = badgeClassFor(item.keywordKind ?? null);
  const badgeClass = styles[badgeKey];
  const href = item.articleLink?.trim() || "";

  return (
    <div
      className={styles.overlay}
      role="presentation"
      onMouseDown={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div
        className={styles.panel}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
      >
        <button
          ref={closeRef}
          type="button"
          className={styles.closeBtn}
          onClick={onClose}
          aria-label="닫기"
        >
          ×
        </button>

        <div className={styles.headRow}>
          <div className={styles.thumb} aria-hidden>
            {label ? (
              <span className={`${styles.thumbBadge} ${badgeClass}`}>
                {label}
              </span>
            ) : (
              thumbLetter(item.title)
            )}
          </div>
          <div className={styles.headMain}>
            <h2 id={titleId} className={styles.title}>
              {item.title}
            </h2>
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
        </div>

        {item.relatedStocks && item.relatedStocks.length > 0 ? (
          <div className={styles.modalChips}>
            <RelatedStockChips
              items={item.relatedStocks}
              size="comfortable"
            />
          </div>
        ) : null}

        <div className={styles.scroll}>
          <p className={styles.previewLabel}>미리보기</p>
          {item.description?.trim() ? (
            <p className={styles.desc}>
              {ensurePreviewTrailingEllipsis(item.description)}
            </p>
          ) : (
            <p className={styles.noDesc}>
              짧은 요약문이 없습니다. 원문에서 전체 기사를 확인할 수 있습니다.
            </p>
          )}
        </div>

        <div className={styles.footer}>
          {href ? (
            <a
              href={href}
              target="_blank"
              rel="noopener noreferrer"
              className={styles.primaryLink}
            >
              원문에서 전체 보기
            </a>
          ) : (
            <span className={styles.primaryDisabled}>원문 링크 없음</span>
          )}
          <p className={styles.hint}>
            전체 기사는 출판사 웹사이트에서 열립니다.
          </p>
        </div>
      </div>
    </div>
  );
}
