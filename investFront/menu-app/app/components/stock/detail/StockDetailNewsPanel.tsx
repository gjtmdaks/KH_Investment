"use client";

import { stripHtml } from "@/lib/stock/stockDetailFormat";
import type { NewsResponse } from "@/lib/stock/stockDetailTypes";

import styles from "./css/stockDetailNews.module.css";
import { StockDetailEmptyState } from "./StockDetailEmptyState";

export function StockDetailNewsPanel({ news }: { news: NewsResponse[] }) {
  if (news.length === 0) {
    return <StockDetailEmptyState title="관련 뉴스가 없습니다." />;
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
