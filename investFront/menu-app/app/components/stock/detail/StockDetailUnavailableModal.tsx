"use client";

import { useEffect, useId, useRef } from "react";

import styles from "./css/stockDetailUnavailableModal.module.css";

type Props = {
  open: boolean;
  onConfirm: () => void;
};

export default function StockDetailUnavailableModal({
  open,
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

  if (!open) {
    return null;
  }

  return (
    <div className={styles.overlay} role="presentation">
      <div
        className={styles.panel}
        role="alertdialog"
        aria-modal="true"
        aria-labelledby={titleId}
      >
        <h2 id={titleId} className={styles.title}>
          지원하지 않거나 상장 폐지된 주식이에요.
        </h2>
        <p className={styles.subtitle}>
          자세한 문의사항은 고객센터를 이용해주세요.
        </p>
        <button
          ref={confirmRef}
          type="button"
          className={styles.confirmBtn}
          onClick={onConfirm}
        >
          확인
        </button>
      </div>
    </div>
  );
}
