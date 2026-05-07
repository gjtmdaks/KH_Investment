import Header from "../components/header/Header";
import MainSidebar from "../components/sidebar/MainSidebar";
import styles from "./MainLayout.module.css";

async function getMainData() {
  const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/main`, {
    cache: "no-store",
    credentials: "include", // JWT 쿠키 대응
  });

  return res.json();
}

export default async function MainLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const data = await getMainData();

  return (
    <div className={styles.pageLayout}>
      <Header data={data} />

      <div className={styles.scrollRegion}>
        <div className={styles.bodyRow}>
          <main className={styles.content}>{children}</main>
          <MainSidebar data={data} />
        </div>
      </div>
    </div>
  );
}