"use client";

import { useEffect, useId, useRef } from "react";

import styles from "./css/stockDetailRiskAckModal.module.css";

type Props = {
  open: boolean;
  stockName: string;
  onConfirm: () => void;
};

export default function StockDetailRiskAckModal({
  open,
  stockName,
  onConfirm,
}: Props) {
  const titleId = useId();
  const confirmRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (!open) {
      return;
    }

    const prevOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    confirmRef.current?.focus();

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
        onConfirm();
      }
    };

    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [open, onConfirm]);

  if (!open) {
    return null;
  }

  return (
    <div
      className={styles.overlay}
      role="presentation"
      onMouseDown={(e) => {
        if (e.target === e.currentTarget) {
          onConfirm();
        }
      }}
    >
      <div
        className={styles.panel}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
      >
        <p className={styles.badge}>ETF 상품 유의사항</p>
        <h2 id={titleId} className={styles.title}>
          {stockName}
        </h2>
        <ul className={styles.list}>
          <li>
            레버리지·인버스 상품은 가격 변동이 크고, 보유 기간에 따라 손실이
            커질 수 있습니다.
          </li>
          <li>
            일별 추적 오차·만기·리밸런싱 등 실제 상품 특성은 이 모의투자에서
            반영되지 않습니다.
          </li>
          <li>본 서비스는 모의투자 시뮬레이션이며, 실제 투자 권유가 아닙니다.</li>
        </ul>
        <button
          ref={confirmRef}
          type="button"
          className={styles.confirmBtn}
          onClick={onConfirm}
        >
          확인했습니다
        </button>
      </div>
    </div>
  );
}
