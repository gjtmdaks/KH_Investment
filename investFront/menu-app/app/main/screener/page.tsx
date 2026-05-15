import styles from "./ScreenerPage.module.css";
import ScreenerHero from "./_components/ScreenerHero";
import ScreenerLayout from "./_components/ScreenerLayout";

async function getData() {
  const base = process.env.NEXT_PUBLIC_API_URL;

  const [rising, falling, watchlist, viewed, volume] = await Promise.all([
    fetch(`${base}/stock/screener/rising`, {
      cache: "no-store",
    }).then((r) => r.json()),

    fetch(`${base}/stock/screener/falling`, {
      cache: "no-store",
    }).then((r) => r.json()),

    fetch(`${base}/stock/screener/watchlist`, {
      cache: "no-store",
    }).then((r) => r.json()),

    fetch(`${base}/stock/screener/viewed`, {
      cache: "no-store",
    }).then((r) => r.json()),

    fetch(`${base}/stock/screener/volume`, {
      cache: "no-store",
    }).then((r) => r.json()),
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