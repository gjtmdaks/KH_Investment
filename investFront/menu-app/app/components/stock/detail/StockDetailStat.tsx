"use client";

import styles from "./css/stockDetailStat.module.css";

export function StockDetailStat({
  label,
  value,
  className,
}: {
  label: string;
  value: string;
  className?: string;
}) {
  return (
    <div className={className ? `${styles.stat} ${className}` : styles.stat}>
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}
