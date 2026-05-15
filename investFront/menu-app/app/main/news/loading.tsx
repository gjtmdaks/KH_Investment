import styles from "./NewsPage.module.css";

export default function NewsLoading() {
  return (
    <div className={styles.pageWrap}>
      <h2 className={styles.sectionTitle}>뉴스</h2>
      <p className={styles.subtitle}>
        최신 반도체·증시·경제 소식 위주로 보여 드립니다.
      </p>
      <div className={styles.skeletonTagBar} aria-hidden>
        {Array.from({ length: 7 }).map((_, i) => (
          <span key={i} className={styles.skeletonTag} />
        ))}
      </div>
      <div className={styles.list}>
        {Array.from({ length: 5 }).map((_, i) => (
          <div key={i} className={styles.skeletonCard}>
            <div className={styles.skeletonThumb} />
            <div className={styles.skeletonBody}>
              <div className={styles.skeletonLineLg} />
              <div className={styles.skeletonLineMd} />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
