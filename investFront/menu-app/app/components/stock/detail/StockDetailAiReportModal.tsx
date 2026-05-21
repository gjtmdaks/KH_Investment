"use client";

import { useEffect, useId, useRef, useState } from "react";
import { createPortal } from "react-dom";

import type { StockAiReport } from "./StockDetailAiPanel";
import styles from "./css/stockDetailAiReportModal.module.css";
import panelStyles from "./css/stockDetailAiPanel.module.css";
import { formatAiReportDate, getAiSignalMeta, getInvestmentOpinionLabel } from "./stockDetailAiPanelUtils";

type Props = {
  open: boolean;
  report: StockAiReport;
  onClose: () => void;
};

export default function StockDetailAiReportModal({
  open,
  report,
  onClose,
}: Props) {
  const titleId = useId();
  const closeRef = useRef<HTMLButtonElement>(null);
  const [mounted, setMounted] = useState(false);
  const { signalClass, signalLabel } = getAiSignalMeta(report.aiSignal);

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (!open) {
      return;
    }

    const prevOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    closeRef.current?.focus();

    return () => {
      document.body.style.overflow = prevOverflow;
    };
  }, [open]);

  useEffect(() => {
    if (!open) {
      return;
    }

    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") {
        onClose();
      }
    };

    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [open, onClose]);

  if (!open || !mounted) {
    return null;
  }

  return createPortal(
    <div
      className={styles.overlay}
      role="presentation"
      onMouseDown={(e) => {
        if (e.target === e.currentTarget) {
          onClose();
        }
      }}
    >
      <div
        className={styles.panel}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
      >
        <div className={styles.panelHeader}>
          <h2 id={titleId} className={styles.panelTitle}>
            AI 종목 분석
          </h2>
          <button
            ref={closeRef}
            type="button"
            className={styles.closeBtn}
            onClick={onClose}
            aria-label="닫기"
          >
            ✕
          </button>
        </div>

        <div className={styles.updatedAt}>
          {formatAiReportDate(report.updatedAt)}
        </div>

        <div className={panelStyles.topSection}>
          <div className={`${panelStyles.signal} ${signalClass}`}>
            {signalLabel}
          </div>

          <div className={panelStyles.opinion}>
            {getInvestmentOpinionLabel(report.investmentOpinion)}
          </div>

          <div className={panelStyles.score}>
            신뢰도 {report.confidenceScore}%
          </div>
        </div>

        <div className={panelStyles.summary}>
          {report.summary}
        </div>

        <div className={panelStyles.factorSection}>
          <div className={panelStyles.factorTitle}>긍정 요인</div>
          <div className={panelStyles.factorContent}>
            {report.positiveFactors}
          </div>
        </div>

        <div className={panelStyles.factorSection}>
          <div className={panelStyles.riskTitle}>리스크</div>
          <div className={panelStyles.factorContent}>
            {report.riskFactors}
          </div>
        </div>
      </div>
    </div>,
    document.body
  );
}
