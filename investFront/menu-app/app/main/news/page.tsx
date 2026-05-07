import Link from "next/link";
import styles from "./NewsPage.module.css";

export const dynamic = "force-dynamic";

type NewsItem = {
  newsInfoId: number | null;
  title: string;
  description: string;
  publisher: string;
  articleLink: string;
  publishedAt: string;
};

function getApiBase(): string {
  const raw = process.env.NEXT_PUBLIC_API_URL?.replace(/\/$/, "")?.trim();
  return raw || "http://localhost:8081";
}

async function fetchMarketNews(): Promise<{ ok: boolean; items: NewsItem[] }> {
  const base = getApiBase();
  try {
    const res = await fetch(`${base}/api/public/news/market?size=28`, {
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

function formatRelativeTimeKo(iso: string): string {
  if (!iso) return "";
  const t = Date.parse(iso);
  if (Number.isNaN(t)) return "";
  const diff = Date.now() - t;
  if (diff < 60_000) return "방금 전";
  const mins = Math.floor(diff / 60_000);
  if (mins < 60) return `${mins}분 전`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}시간 전`;
  const days = Math.floor(hrs / 24);
  if (days < 7) return `${days}일 전`;
  return new Date(t).toLocaleDateString("ko-KR", {
    month: "short",
    day: "numeric",
  });
}

function thumbLetter(title: string): string {
  const t = title?.trim();
  if (!t) return "·";
  const ch = t[0];
  if (/[a-zA-Z]/.test(ch)) return ch.toUpperCase();
  return ch;
}

function shortenHost(publisher: string): string {
  if (!publisher || publisher === "-") return "출처 미상";
  return publisher.replace(/^www\./, "");
}

export default async function NewsPage() {
  const { ok, items } = await fetchMarketNews();

  return (
    <>
      <h2 className={styles.sectionTitle}>뉴스</h2>
      <p className={styles.subtitle}>
        증시·경제·기업 소식 위주로 보여 드립니다. 기사를 누르면 원문으로
        이동합니다.
      </p>

      {!ok ? (
        <div className={styles.error}>
          뉴스를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.
        </div>
      ) : items.length === 0 ? (
        <div className={styles.empty}>
          표시할 뉴스가 없습니다. API 키·네트워크 또는 잠시 후 다시 확인해
          주세요.
        </div>
      ) : (
        <div className={styles.list} role="list">
          {items.map((item, idx) => (
            <Link
              key={`${item.articleLink || "n"}-${idx}`}
              href={item.articleLink || "#"}
              target="_blank"
              rel="noopener noreferrer"
              className={styles.card}
              role="listitem"
            >
              <div className={styles.thumb} aria-hidden>
                {thumbLetter(item.title)}
              </div>
              <div className={styles.body}>
                <h3 className={styles.title}>{item.title}</h3>
                {item.description ? (
                  <p className={styles.desc}>{item.description}</p>
                ) : null}
                <div className={styles.meta}>
                  <span className={styles.publisher}>
                    {shortenHost(item.publisher)}
                  </span>
                  <span className={styles.dot}>·</span>
                  <span className={styles.time}>
                    {formatRelativeTimeKo(item.publishedAt)}
                  </span>
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </>
  );
}
