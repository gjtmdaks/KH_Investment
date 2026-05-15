"use client";

import styles from "./css/stockDetailEmptyState.module.css";

export function StockDetailEmptyState({ title }: { title: string }) {
  return <div className={styles.empty}>{title}</div>;
}
