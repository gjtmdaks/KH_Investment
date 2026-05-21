"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import styles from "../myAccount.module.css";
import { apiClient } from "@/lib/api-client";

type UserAiProfileResponse = {
  surveyType: string;
  actualInvestmentType: string;
  portfolioRiskAnalysis: string[];
  aiRecommendations: string[];
  updatedAt: string;
};

export default function UserAiProfilePanel() {
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [analyzing, setAnalyzing] = useState(false);
  const [report, setReport] = useState<UserAiProfileResponse | null>(null);
  const [hasSurvey, setHasSurvey] = useState(true);

  useEffect(() => {
    fetchProfile();
  }, []);

  async function fetchProfile() {
    try {
      setLoading(true);

      const response = await apiClient.get(
        "/api/ai/user-profile"
      );

      setReport(response.data);
      setHasSurvey(true);

    } catch (e: any) {
      if (e?.response?.status === 404) {
        setReport(null);
        return;
      }

      if (e?.response?.status === 400) {
        setHasSurvey(false);
        return;
      }

    } finally {
      setLoading(false);
    }
  }

  async function handleAnalyze() {
    try {
      setAnalyzing(true);

      await apiClient.post(
        "/api/ai/user-profile/analyze"
      );

      await fetchProfile();

    } catch (e) {
      console.error(e);

    } finally {
      setAnalyzing(false);
    }
  }

  if (loading) {
    return (
      <div className={styles.aiProfileLoading}>
        AI 투자 성향 분석 정보를 불러오는 중입니다.
      </div>
    );
  }

  if (!hasSurvey) {
    return (
      <div className={styles.aiProfileEmpty}>
        <div className={styles.aiProfileTitle}>
          AI 투자 성향 분석
        </div>

        <p className={styles.aiProfileDesc}>
          투자 성향 설문을 완료하면
          AI 기반 포트폴리오 분석을 받을 수 있습니다.
        </p>

        <button
          className={styles.aiProfileButton}
          onClick={() =>
            router.push(
              "/main/myPage/member/investment-type"
            )
          }
        >
          투자 성향 설문 하러가기
        </button>
      </div>
    );
  }

  if (!report) {
    return (
      <div className={styles.aiProfileEmpty}>
        <div className={styles.aiProfileTitle}>
          AI 투자 성향 분석
        </div>

        <p className={styles.aiProfileDesc}>
          현재 보유 자산과 투자 성향을 기반으로
          AI 분석을 생성할 수 있습니다.
        </p>

        <button
          className={styles.aiProfileButton}
          onClick={handleAnalyze}
          disabled={analyzing}
        >
          {analyzing
            ? "AI 분석 생성 중..."
            : "AI 분석 시작"}
        </button>
      </div>
    );
  }

  return (
    <div className={styles.aiProfileWrapper}>
      <div className={styles.aiProfileHeader}>
        <div>
          <h2>AI 투자 성향 분석</h2>

          <p>
            현재 포트폴리오와 투자 패턴 기반 분석
          </p>
        </div>

        <button
          className={styles.aiRefreshButton}
          onClick={handleAnalyze}
          disabled={analyzing}
        >
          재분석
        </button>
      </div>

      <div className={styles.aiSurveyType}>
        설문 성향
        <strong>{report.surveyType}</strong>
      </div>

      <section className={styles.aiSection}>
        <h3>실제 투자 성향 분석</h3>

        <p>
          {report.actualInvestmentType}
        </p>
      </section>

      <section className={styles.aiSection}>
        <h3>포트폴리오 위험 분석</h3>

        <ul className={styles.aiList}>
          {report?.portfolioRiskAnalysis.map((item) => (
            <li key={item}>
              {item}
            </li>
          ))}
        </ul>
      </section>

      <section className={styles.aiSection}>
        <h3>AI 제안</h3>

        <ul className={styles.aiList}>
          {report.aiRecommendations.map((item) => (
            <li key={item}>
              {item}
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}