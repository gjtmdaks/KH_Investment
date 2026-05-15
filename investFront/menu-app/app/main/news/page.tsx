import { getPublicApiBase } from "@/lib/api-base";

import NewsFeedClient from "./NewsFeedClient";
import type { NewsItem } from "./newsTypes";
import styles from "./NewsPage.module.css";

export const dynamic = "force-dynamic";

const NEWS_MARKET_SSR_SIZE = 56;

async function fetchMarketNews(
  limit: number,
): Promise<{ ok: boolean; items: NewsItem[] }> {
  const base = getPublicApiBase();
  try {
    const res = await fetch(
      `${base}/api/public/news/market?size=${limit}`,
      {
        cache: "no-store",
        credentials: "include",
      },
    );
    if (!res.ok) {
      return { ok: false, items: [] };
    }
    const data = (await res.json()) as NewsItem[];
    if (!Array.isArray(data)) {
      return { ok: true, items: [] };
    }
    return { ok: true, items: data };
  } catch {
    return { ok: false, items: [] };
  }
}

export default async function NewsPage() {
  const { ok, items } = await fetchMarketNews(NEWS_MARKET_SSR_SIZE);

  return (
    <div className={styles.pageWrap}>
      <h2 className={styles.sectionTitle}>뉴스</h2>
      <p className={styles.subtitle}>
        최신 반도체·증시·경제 소식 위주로 보여 드립니다. 기사를 누르면 미리보기
        창이 열리며, 전체 기사는 원문 링크에서 확인할 수 있습니다.
      </p>

      <NewsFeedClient ok={ok} items={items} />
    </div>
  );
}
