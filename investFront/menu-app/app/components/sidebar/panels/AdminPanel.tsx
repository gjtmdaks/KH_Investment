"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { apiClient } from "@/lib/api-client";
import styles from "../MainSidebar.module.css";

export default function AdminPanel() {
  const router = useRouter();
  
  const [companyLoading, setCompanyLoading] = useState(false);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [historyStop, setHistoryStop] = useState(false);

  async function requestCompanySync() {
    try {
      setCompanyLoading(true);

      await apiClient.post("/admin/api/dart/init");

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

      await apiClient.post("/admin/api/kis/historysync");

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

      await apiClient.post("/admin/api/kis/historysyncstop");

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

      <button
        className={styles.addButton}
        onClick={() => router.push("/main/admin/user-management")}
      >
        회원 관리
      </button>
    </div>
  );
}