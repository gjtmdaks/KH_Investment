import styles from "./ScreenerPage.module.css";
import ScreenerHero from "./_components/ScreenerHero";
import ScreenerLayout from "./_components/ScreenerLayout";
import { getPublicApiBase } from "@/lib/api-base";
import { StockItem } from "./_components/types";

async function fetchScreenerStocks(
  apiBase: string,
  path: string
): Promise<StockItem[]> {
  try {
    const res = await fetch(`${apiBase}${path}`, {
      cache: "no-store",
    });

    const contentType = res.headers.get("content-type") ?? "";

    if (!res.ok || !contentType.includes("application/json")) {
      return [];
    }

    const data = await res.json();

    return Array.isArray(data) ? data : [];
  } catch {
    return [];
  }
}

async function getData() {
  const base = getPublicApiBase();

  const [rising, falling, watchlist, viewed, volume] = await Promise.all([
    fetchScreenerStocks(base, "/stock/screener/rising"),
    fetchScreenerStocks(base, "/stock/screener/falling"),
    fetchScreenerStocks(base, "/stock/screener/watchlist"),
    fetchScreenerStocks(base, "/stock/screener/viewed"),
    fetchScreenerStocks(base, "/stock/screener/volume"),
  ]);

  return {
    rising,
    falling,
    watchlist,
    viewed,
    volume,
  };
}

export default async function ScreenerPage() {
  const data = await getData();

  return (
    <div className={styles.container}>
      <ScreenerHero />

      <ScreenerLayout data={data} />
    </div>
  );
}