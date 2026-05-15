import styles from "../ScreenerPage.module.css";

export default function ScreenerHero() {
  return (
    <section className={styles.hero}>
      <div>
        <h1 className={styles.heroTitle}>주식 골라보기</h1>

        <p className={styles.heroDesc}>
          상승 추세, 인기 종목, 거래량 급증 종목을 빠르게 탐색해보세요.
        </p>
      </div>
    </section>
  );
}