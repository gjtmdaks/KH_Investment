"use client";

import { useState } from "react";

import StockDetailAiReportModal from "./StockDetailAiReportModal";
import styles from "./css/stockDetailAiPanel.module.css";
import {
  formatAiReportDate,
  getAiSignalMeta,
  getInvestmentOpinionLabel,
} from "./stockDetailAiPanelUtils";

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
  const [modalOpen, setModalOpen] = useState(false);

  if (loading) {
    return (
      <section className={styles.card}>
        <div className={styles.loadingCompact}>AI 리포트 생성 중...</div>
      </section>
    );
  }

  if (!report) {
    return (
      <section className={styles.card}>
        <div className={styles.emptyCompact}>AI 분석 데이터가 없습니다.</div>
      </section>
    );
  }

  const { signalClass, signalLabel } = getAiSignalMeta(report.aiSignal);

  return (
    <>
      <button
        type="button"
        className={`${styles.card} ${styles.compactCard}`}
        onClick={() => setModalOpen(true)}
        aria-label="AI 종목 분석 전체 보기"
      >
        <div className={styles.headerRow}>
          <div className={styles.header}>AI 종목 분석</div>
          <div className={styles.updatedAt}>
            {formatAiReportDate(report.updatedAt)}
          </div>
        </div>

        <div className={styles.topSection}>
          <div className={`${styles.signal} ${signalClass}`}>
            {signalLabel}
          </div>

          <div className={styles.opinion}>
            {getInvestmentOpinionLabel(report.investmentOpinion)}
          </div>

          <div className={styles.score}>
            신뢰도 {report.confidenceScore}%
          </div>
        </div>

        <p className={styles.summaryPreview}>{report.summary}</p>
      </button>

      <StockDetailAiReportModal
        open={modalOpen}
        report={report}
        onClose={() => setModalOpen(false)}
      />
    </>
  );
}
