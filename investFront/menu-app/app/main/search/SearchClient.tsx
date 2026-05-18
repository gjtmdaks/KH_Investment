"use client";

import { useState } from "react";
import SearchTabs from "./SearchTabs";
import SearchStockList from "./SearchStockList";
import SearchNewsList from "./SearchNewsList";
import type {
  StockItem,
  NewsItem,
} from "./page";

export default function SearchClient({
  stocks,
  news,
}: {
  keyword: string;
  stocks: StockItem[];
  news: NewsItem[];
}) {
  const [tab, setTab] = useState<
    "stock" | "news"
  >("stock");

  return (
    <>
      <SearchTabs
        activeTab={tab}
        stockCount={stocks.length}
        newsCount={news.length}
        onChange={setTab}
      />

      {tab === "stock" ? (
        <SearchStockList
          stocks={stocks}
        />
      ) : (
        <SearchNewsList
          news={news}
        />
      )}
    </>
  );
}