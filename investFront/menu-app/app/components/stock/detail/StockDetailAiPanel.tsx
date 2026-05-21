import styles from "./css/stockDetailAiPanel.module.css";

export type StockAiReport = {
  stockCode: string;

  investmentOpinion: string;
  confidenceScore: number;

  summary: string;
  riskFactors: string;
  positiveFactors: string;

  aiSignal: string;
  updatedAt: string;
};

export function StockDetailAiPanel({
  report,
  loading,
}: {
  report: StockAiReport | null;
  loading?: boolean;
}) {

  if (loading) {
    return (
      <section className={styles.card}>
        <div className={styles.loading}>
          AI 리포트 생성 중...
        </div>
      </section>
    );
  }

  if (!report) {
    return (
      <section className={styles.card}>
        <div className={styles.empty}>
          AI 분석 데이터가 없습니다.
        </div>
      </section>
    );
  }

  const signalClass =
    report.aiSignal === "POSITIVE"
      ? styles.positive
      : report.aiSignal === "NEGATIVE"
        ? styles.negative
        : styles.neutral;

  const signalLabel =
    report.aiSignal === "POSITIVE"
      ? "🟢 긍정"
      : report.aiSignal === "NEGATIVE"
        ? "🔴 부정"
        : "🟡 중립";

  return (
    <section className={styles.card}>
      <div className={styles.headerRow}>
        <div className={styles.header}>
          AI 종목 분석
        </div>

        <div className={styles.updatedAt}>
          {formatDate(report.updatedAt)}
        </div>
      </div>

      <div className={styles.topSection}>
        <div className={`${styles.signal} ${signalClass}`}>
          {signalLabel}
        </div>

        <div className={styles.opinion}>
          {
            report.investmentOpinion === "BUY"
              ? "매수"
              : report.investmentOpinion === "SELL"
                ? "매도"
                : "관망"
          }
        </div>

        <div className={styles.score}>
          신뢰도 {report.confidenceScore}%
        </div>
      </div>

      <div className={styles.summary}>
        {report.summary}
      </div>

      <div className={styles.factorSection}>
        <div className={styles.factorTitle}>
          긍정 요인
        </div>

        <div className={styles.factorContent}>
          {report.positiveFactors}
        </div>
      </div>

      <div className={styles.factorSection}>
        <div className={styles.riskTitle}>
          리스크
        </div>

        <div className={styles.factorContent}>
          {report.riskFactors}
        </div>
      </div>
    </section>
  );
}

function formatDate(date: any) {
  if (!date) return "";
  
  const d = new Date(date);
  if (isNaN(d.getTime())) return "";

  return new Intl.DateTimeFormat("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  }).format(d);
}
