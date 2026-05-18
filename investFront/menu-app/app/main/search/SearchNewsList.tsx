"use client";

import { useState } from "react";
import styles from "./SearchNewsList.module.css";
import NewsArticleModal from "@/app/main/news/NewsArticleModal";
import type { NewsItem } from "./page";

function formatDate(dateString: string) {
  return new Intl.DateTimeFormat(
    "ko-KR",
    {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
    }
  ).format(new Date(dateString));
}

export default function SearchNewsList({
  news,
}: {
  news: NewsItem[];
}) {
  const [selectedNews, setSelectedNews] =
    useState<NewsItem | null>(null);

  if (news.length === 0) {
    return (
      <div className={styles.empty}>
        검색된 뉴스가 없습니다.
      </div>
    );
  }

  return (
    <>
      <div className={styles.list}>
        {news.map((item) => (
          <button
            key={item.newsInfoId}
            type="button"
            className={styles.item}
            onClick={() => setSelectedNews(item)}
          >
            <div className={styles.top}>
              <div className={styles.newsTitle}>
                {item.newsTitle}
              </div>

              <div className={styles.date}>
                {formatDate(item.publishedAt)}
              </div>
            </div>

            <div className={styles.summary}>
              {item.newsDescription}
            </div>
          </button>
        ))}
      </div>

      <NewsArticleModal
        item={
          selectedNews
            ? {
                newsInfoId: selectedNews.newsInfoId,

                title: selectedNews.newsTitle,

                description: selectedNews.newsDescription,

                publishedAt: selectedNews.publishedAt,

                articleLink: selectedNews.articleLink,

                publisher: selectedNews.publisher,

                keywordKind: null,

                primaryLabel: null,

                relatedStocks: [],
              }
            : null
        }
        onClose={() => setSelectedNews(null)}
      />
    </>
  );
}