import styles from "./SearchTabs.module.css";

export default function SearchTabs({
  activeTab,
  stockCount,
  newsCount,
  onChange,
}: {
  activeTab: "stock" | "news";
  stockCount: number;
  newsCount: number;
  onChange: (
    tab: "stock" | "news"
  ) => void;
}) {

  return (
    <div className={styles.tabs}>
      <button
        className={
          activeTab === "stock"
            ? styles.activeTab
            : styles.tab
        }
        onClick={() => onChange("stock")}
      >
        종목 ({stockCount})
      </button>

      <button
        className={
          activeTab === "news"
            ? styles.activeTab
            : styles.tab
        }
        onClick={() => onChange("news")}
      >
        뉴스 ({newsCount})
      </button>
    </div>
  );
}