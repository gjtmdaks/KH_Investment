import styles from "./css/stockDetailAiPanel.module.css";

export function StockDetailAiPanel() {
  return (
    <section className={styles.card}>
      <div className={styles.header}>
        AI 종목 분석
      </div>

      <div className={styles.sentiment}>
        🟢 긍정
      </div>

      <div className={styles.summary}>
        최근 AI 반도체 수요 확대와 외국인 수급 유입으로
        단기 상승 흐름이 강화되고 있습니다.
      </div>

      <div className={styles.score}>
        신뢰도 89%
      </div>
    </section>
  );
}