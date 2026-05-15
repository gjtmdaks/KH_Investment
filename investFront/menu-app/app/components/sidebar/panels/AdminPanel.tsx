"use client";

import { useState } from "react";
import styles from "../MainSidebar.module.css";

const rawBase = process.env.NEXT_PUBLIC_API_URL?.replace(/\/$/, "") ?? "";
const apiBase = rawBase.trim() || "http://localhost:8081/final";

export default function AdminPanel() {
  const [companyLoading, setCompanyLoading] = useState(false);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [historyStop, setHistoryStop] = useState(false);

  async function requestCompanySync() {
    try {
      setCompanyLoading(true);

      const response = await fetch(
        `${apiBase}/admin/api/dart/init`,
        {
          method: "POST",
        }
      );

      if (!response.ok) {
        throw new Error(
          "회사 정보 동기화 실패"
        );
      }

      alert(
        "회사 정보 동기화 시작"
      );
    } catch (e) {
      console.error(e);
      alert(
        "회사 정보 동기화 실패"
      );
    } finally {
      setCompanyLoading(false);
    }
  }

  async function requestHistorySync() {
    try {
      setHistoryLoading(true);

      const response = await fetch(
        `${apiBase}/admin/api/kis/historysync`,
        {
          method: "POST",
        }
      );

      if (!response.ok) {
        throw new Error(
          "과거 시세 동기화 실패"
        );
      }

      alert(
        "과거 시세 동기화 시작"
      );
    } catch (e) {
      console.error(e);
      alert(
        "과거 시세 동기화 실패"
      );
    } finally {
      setHistoryLoading(false);
    }
  }

  async function requestHistorySyncStop() {
    try {
      setHistoryStop(true);

      const response = await fetch(
        `${apiBase}/admin/api/kis/historysyncstop`,
        {
          method: "POST",
        }
      );

      if (!response.ok) {
        throw new Error(
          "과거 시세 동기화 중지 실패"
        );
      }

      alert(
        "과거 시세 동기화 중지 시작"
      );
    } catch (e) {
      console.error(e);
      alert(
        "과거 시세 동기화 중지 실패"
      );
    } finally {
      setHistoryStop(false);
    }
  }

  return (
    <div className={styles.panelContent}>
      <button
        className={styles.addButton}
        onClick={requestCompanySync}
        disabled={companyLoading}
      >
        회사 정보 동기화
      </button>

      <button
        className={styles.addButton}
        onClick={requestHistorySync}
        disabled={historyLoading}
      >
        과거 시세 동기화
      </button>

      <button
        className={styles.addButton}
        onClick={requestHistorySyncStop}
        disabled={historyStop}
      >
        과거 시세 동기화 중지
      </button>
    </div>
  );
}