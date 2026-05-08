import NewsFeedClient, { type NewsItem } from "./NewsFeedClient";
import styles from "./NewsPage.module.css";

export const dynamic = "force-dynamic";

function getApiBase(): string {
  const raw = process.env.NEXT_PUBLIC_API_URL?.replace(/\/$/, "")?.trim();
  return raw || "http://localhost:8081";
}

async function fetchMarketNews(): Promise<{ ok: boolean; items: NewsItem[] }> {
  const base = getApiBase();
  try {
    const res = await fetch(`${base}/api/public/news/market?size=100`, {
      cache: "no-store",
      credentials: "include",
    });
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
  const { ok, items } = await fetchMarketNews();

  return (
    <div className={styles.pageWrap}>
      <h2 className={styles.sectionTitle}>뉴스</h2>
      <p className={styles.subtitle}>
        반도체·증시·경제 소식 위주로 보여 드립니다. 기사를 누르면 원문으로
        이동합니다.
      </p>

      <NewsFeedClient ok={ok} items={items} />
    </div>
  );
}
