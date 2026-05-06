import Header from "../components/header/Header";
import MainSidebar from "../components/sidebar/MainSidebar";
import styles from "./MainLayout.module.css";

export default function MainLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className={styles.pageLayout}>
      <div className={styles.leftArea}>
        <Header />
        <main className={styles.content}>{children}</main>
      </div>
      <MainSidebar />
      
    </div>
  );
}