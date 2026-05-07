"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Suspense, useEffect, useMemo, useState } from "react";

import styles from "./oauthCallback.module.css";

type Phase = "pending" | "working" | "done" | "error";

function looksLikeJwt(token: string): boolean {
  const parts = token.split(".");
  return (
    parts.length === 3 && parts.every((p) => p.length > 0 && !p.includes(" "))
  );
}

function OAuthCallbackBody() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [phase, setPhase] = useState<Phase>("pending");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const oauthError = useMemo(
    () => searchParams.get("error"),
    [searchParams]
  );
  const oauthErrorDescription = useMemo(
    () => searchParams.get("error_description"),
    [searchParams]
  );
  const accessToken = useMemo(
    () => searchParams.get("accessToken"),
    [searchParams]
  );

  useEffect(() => {
    if (phase !== "pending") {
      return;
    }

    if (oauthError) {
      setErrorMessage(
        oauthErrorDescription?.trim() ||
          "소셜 로그인이 중단되었거나 거부되었습니다."
      );
      setPhase("error");
      return;
    }

    const raw = accessToken?.trim();
    if (!raw) {
      setErrorMessage(
        "로그인 정보가 전달되지 않았습니다. 다시 로그인해 주세요."
      );
      setPhase("error");
      return;
    }

    if (!looksLikeJwt(raw)) {
      setErrorMessage("유효하지 않은 토큰입니다. 다시 로그인해 주세요.");
      setPhase("error");
      return;
    }

    setPhase("working");
    try {
      window.localStorage.setItem("accessToken", raw);
    } catch {
      setErrorMessage(
        "브라우저 저장소에 접근할 수 없습니다. 쿠키·저장 설정을 확인해 주세요."
      );
      setPhase("error");
      return;
    }

    setPhase("done");
    router.replace("/main");
  }, [phase, accessToken, oauthError, oauthErrorDescription, router]);

  const showSpinner = phase === "pending" || phase === "working" || phase === "done";

  return (
    <main className={styles.container}>
      <div className={styles.card}>
        <p className={styles.brand}>KH 증권</p>
        <h1 className={styles.title}>카카오 로그인</h1>

        {showSpinner ? (
          <>
            <p className={styles.message}>
              {phase === "done"
                ? "메인 화면으로 이동 중입니다."
                : "로그인 정보를 확인하는 중입니다."}
            </p>
            <div className={styles.spinnerWrap} aria-live="polite">
              <div className={styles.spinner} aria-hidden />
            </div>
          </>
        ) : null}

        {phase === "error" ? (
          <>
            <p className={styles.message}>로그인을 완료할 수 없습니다.</p>
            {errorMessage ? (
              <p className={styles.error} role="alert">
                {errorMessage}
              </p>
            ) : null}
            <div className={styles.actions}>
              <Link href="/sign-in" className={styles.primaryLink}>
                로그인 페이지로 이동
              </Link>
              <Link href="/main" className={styles.secondaryLink}>
                메인으로 가기 (비로그인)
              </Link>
            </div>
          </>
        ) : null}

        <p className={styles.hint}>
          이 페이지는 카카오 인증 후에만 표시됩니다. 잠시만 기다려 주세요.
        </p>
      </div>
    </main>
  );
}

function OAuthCallbackFallback() {
  return (
    <main className={styles.container}>
      <div className={styles.card}>
        <p className={styles.brand}>KH 증권</p>
        <h1 className={styles.title}>카카오 로그인</h1>
        <p className={styles.message}>페이지를 불러오는 중입니다.</p>
        <div className={styles.spinnerWrap} aria-live="polite">
          <div className={styles.spinner} aria-hidden />
        </div>
      </div>
    </main>
  );
}

export default function OAuthCallbackPage() {
  return (
    <Suspense fallback={<OAuthCallbackFallback />}>
      <OAuthCallbackBody />
    </Suspense>
  );
}
