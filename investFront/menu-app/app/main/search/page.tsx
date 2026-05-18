import { notFound } from "next/navigation";
import styles from "./SearchPage.module.css";
import { API_BASE_URL } from "@/lib/api-base";
import SearchClient from "./SearchClient";

export type StockItem = {
  stockCode: string;
  stockName: string;
  marketType: string;
};

export type NewsItem = {
  newsInfoId: number;
  newsTitle: string;
  newsDescription: string;
  publishedAt: string;
  articleLink: string;
  publisher: string;
};

export type SearchResponse = {
  stocks: StockItem[];
  news: NewsItem[];
};

async function search(
  keyword: string
): Promise<SearchResponse> {

  const response = await fetch(
    `${API_BASE_URL}/search?keyword=${encodeURIComponent(keyword)}`,
    {
      cache: "no-store",
    }
  );

  if (!response.ok) {
    throw new Error("검색 실패");
  }

  return response.json();
}

export default async function SearchPage({
  searchParams,
}: {
  searchParams: Promise<{
    q?: string;
  }>;
}) {
  const resolvedSearchParams = await searchParams;
  const keyword = resolvedSearchParams.q?.trim() ?? "";

  if (!keyword) {
    notFound();
  }

  const data = await search(keyword);

  return (
    <main className={styles.page}>
      <div className={styles.container}>
        <h1 className={styles.pageTitle}>
          "{keyword}" 검색 결과
        </h1>

        <SearchClient
          keyword={keyword}
          stocks={data.stocks}
          news={data.news}
        />
      </div>
    </main>
  );
}