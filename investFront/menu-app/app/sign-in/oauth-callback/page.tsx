"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Suspense, useEffect, useMemo, useState } from "react";

import { useAuth } from "@/app/context/AuthContext";

import styles from "./oauthCallback.module.css";

const OAUTH_MESSAGE_SOURCE = "kh-investment-oauth";

type Phase = "pending" | "working" | "done" | "error";

function tryNotifyOAuthPopupSuccess(): boolean {
  if (typeof window === "undefined") {
    return false;
  }
  try {
    if (window.opener && !window.opener.closed) {
      window.opener.postMessage(
        { source: OAUTH_MESSAGE_SOURCE, ok: true },
        window.location.origin
      );
      window.close();
      return true;
    }
  } catch {
    // ignore
  }
  return false;
}

function OAuthCallbackBody() {
  const router = useRouter();
  const { refreshUser } = useAuth();
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

  useEffect(() => {
    if (phase !== "pending") {
      return;
    }

    if (searchParams.has("accessToken")) {
      const next = new URLSearchParams(searchParams.toString());
      next.delete("accessToken");
      const query = next.toString();
      router.replace(
        query ? `/sign-in/oauth-callback?${query}` : "/sign-in/oauth-callback"
      );
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

    setPhase("working");

    async function completeLogin() {
      try {
        const user = await refreshUser();
        if (!user) {
          setErrorMessage(
            "로그인 정보를 불러오지 못했습니다. 다시 로그인해 주세요."
          );
          setPhase("error");
          return;
        }

        setPhase("done");
        if (tryNotifyOAuthPopupSuccess()) {
          return;
        }
        router.replace("/main");
      } catch {
        setErrorMessage(
          "로그인 처리 중 오류가 발생했습니다. 다시 로그인해 주세요."
        );
        setPhase("error");
      }
    }

    void completeLogin();
  }, [phase, oauthError, oauthErrorDescription, refreshUser, router, searchParams]);

  const showSpinner =
    phase === "pending" || phase === "working" || phase === "done";

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
