"use client";

import Link from "next/link";

import { formatPercent, parseNumeric } from "@/lib/stock/stockDetailFormat";

import type { RelatedStock } from "./newsTypes";
import styles from "./RelatedStockChips.module.css";

type Size = "compact" | "comfortable";
type Variant = "default" | "inline";

type Props = {
  items?: RelatedStock[] | null;
  size?: Size;
  ariaLabel?: string;
  variant?: Variant;
  /** `/api/stocks/{code}/price` 등으로 조회한 등락률 문자열(KIS). 종목코드 키 */
  liveChangeRateByCode?: Record<string, string | null | undefined> | null;
};

export default function RelatedStockChips({
  items,
  size = "compact",
  ariaLabel = "관련 종목",
  variant = "default",
  liveChangeRateByCode,
}: Props) {
  if (!items || items.length === 0) {
    return null;
  }

  const chipClass =
    size === "comfortable"
      ? `${styles.chip} ${styles.chipComfortable}`
      : styles.chip;

  const stopBubble: React.MouseEventHandler<HTMLDivElement> = (e) => {
    e.stopPropagation();
  };

  return (
    <div
      className={variant === "inline" ? `${styles.row} ${styles.rowInline}` : styles.row}
      role="list"
      aria-label={ariaLabel}
      onClick={stopBubble}
      onMouseDown={stopBubble}
    >
      {items.map((it, idx) => {
        const code = it.stockCode?.trim();
        const rate = resolveNumericChangeRate(it, liveChangeRateByCode);
        const toneClass =
          rate == null
            ? styles.chipToneMuted
            : rate > 0
              ? styles.chipTonePositive
              : rate < 0
                ? styles.chipToneNegative
                : styles.chipToneFlat;
        const rateClass =
          rate == null
            ? styles.rateFlat
            : rate > 0
              ? styles.ratePositive
              : rate < 0
                ? styles.rateNegative
                : styles.rateFlat;
        const rateText =
          rate == null ? null : formatPercentDisplay(rate);

        const chipInner = (
          <>
            <span className={styles.chipName}>{it.stockName}</span>
            {rateText ? (
              <span className={`${styles.chipRate} ${rateClass}`}>
                {rateText}
              </span>
            ) : null}
          </>
        );

        const stopChipBubble: React.MouseEventHandler = (e) => {
          e.stopPropagation();
        };

        if (code) {
          return (
            <Link
              key={`${code}-${it.stockName}`}
              href={`/main/stock/${encodeURIComponent(code)}`}
              className={`${chipClass} ${toneClass} ${styles.chipLink}`}
              role="listitem"
              aria-label={`${it.stockName} 종목 상세`}
              onClick={stopChipBubble}
              onMouseDown={stopChipBubble}
            >
              {chipInner}
            </Link>
          );
        }

        return (
          <span
            key={`n-${idx}-${it.stockName}`}
            className={`${chipClass} ${toneClass}`}
            role="listitem"
          >
            {chipInner}
          </span>
        );
      })}
    </div>
  );
}

function resolveNumericChangeRate(
  it: RelatedStock,
  live?: Record<string, string | null | undefined> | null,
): number | null {
  const code = it.stockCode?.trim();
  if (code && live) {
    const raw = live[code];
    if (raw != null && String(raw).trim() !== "") {
      const n = parseNumeric(String(raw));
      if (n !== null && Number.isFinite(n)) return n;
    }
  }

  const cr = it.changeRate;
  if (cr == null) return null;
  if (typeof cr === "number") {
    return Number.isFinite(cr) ? cr : null;
  }
  const n = parseNumeric(String(cr));
  return n !== null && Number.isFinite(n) ? n : null;
}

/** StockDetailHero의 formatPercent와 동일 규칙(+/-, 소수 둘째 자리) */
function formatPercentDisplay(rate: number): string {
  if (!Number.isFinite(rate)) return "";
  const s = formatPercent(String(rate));
  return s === "-" ? "" : s;
}
