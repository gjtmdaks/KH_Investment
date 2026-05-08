"use client";

import type { RelatedStock } from "./newsTypes";
import styles from "./RelatedStockChips.module.css";

type Size = "compact" | "comfortable";
type Variant = "default" | "inline";

type Props = {
  items?: RelatedStock[] | null;
  size?: Size;
  ariaLabel?: string;
  variant?: Variant;
};

export default function RelatedStockChips({
  items,
  size = "compact",
  ariaLabel = "관련 종목",
  variant = "default",
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
      {items.map((it) => {
        const rate = typeof it.changeRate === "number" ? it.changeRate : null;
        const rateClass =
          rate == null
            ? styles.rateFlat
            : rate > 0
              ? styles.ratePositive
              : rate < 0
                ? styles.rateNegative
                : styles.rateFlat;
        const rateText = rate == null ? null : formatRate(rate);
        return (
          <span
            key={`${it.stockCode}-${it.stockName}`}
            className={chipClass}
            role="listitem"
          >
            <span className={styles.chipName}>{it.stockName}</span>
            {rateText ? (
              <span className={`${styles.chipRate} ${rateClass}`}>
                {rateText}
              </span>
            ) : null}
          </span>
        );
      })}
    </div>
  );
}

/** 0.0% / +1.91% / -1.40% 처럼 부호와 소수 둘째 자리까지 고정 */
function formatRate(rate: number): string {
  if (!Number.isFinite(rate)) return "";
  if (rate === 0) return "0.00%";
  const sign = rate > 0 ? "+" : "";
  return `${sign}${rate.toFixed(2)}%`;
}
