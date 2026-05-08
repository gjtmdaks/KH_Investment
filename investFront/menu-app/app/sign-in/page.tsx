"use client";
import Link from "next/link";

import Image from "next/image";
import { useRouter } from "next/navigation";
import { FormEvent, useState } from "react";
import { apiClient } from "@/lib/api-client";
import styles from "./signIn.module.css";

const apiBase = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8081";
const kakaoOAuthStartUrl = `${apiBase.replace(/\/$/, "")}/oauth2/authorization/kakao`;

type SignInResponse = {
  accessToken: string;
  userNo: number;
  userId: string;
  userName: string;
  email: string;
  phone: string;
  provider: string;
  auth: number;
};

type ApiEnvelope<T> = {
  success: boolean;
  data: T;
  message?: string | null;
};

export default function SignInPage() {
  const router = useRouter();
  const [userId, setUserId] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  

  async function handleCredentialLogin(e: FormEvent) {
  e.preventDefault();
  setError(null);

  if (!userId.trim()) {
    setError("아이디를 입력해주세요.");
    return;
  }

  if (!password.trim()) {
    setError("비밀번호를 입력해주세요.");
    return;
  }

  setLoading(true);

  try {
    const { data } = await apiClient.post<ApiEnvelope<SignInResponse>>(
      "/users/signin",
      { userId, password }
    );

    if (!data.success || !data.data?.accessToken) {
      setError(data.message ?? "로그인에 실패했습니다.");
      return;
    }

    window.localStorage.setItem("accessToken", data.data.accessToken);
    window.localStorage.setItem("user", JSON.stringify(data.data));

    router.push("/main");
  } catch {
    setError("서버와 통신할 수 없습니다.");
  } finally {
    setLoading(false);
  }
}

  return (
    <main className={styles.container}>
      <div className={styles.topBar}>
        <Link href="/main" className={styles.logoArea}>
          <Image
            src="/logo-full.png"
            alt="KH 증권 로고"
            width={132}
            height={33}
            className={styles.logoImage}
            priority
          />
        </Link>
        <Link href="/sign-up" className={styles.signUpLink}>
          회원가입
        </Link>
      </div>
      
      <section className={styles.card}>
        <p className={styles.brand}>KH 증권</p>
        <h1 className={styles.title}>로그인</h1>
        <p className={styles.description}>
          계정으로 로그인하거나 카카오로 시작할 수 있습니다.
        </p>
        <form className={styles.form} onSubmit={handleCredentialLogin}>
          {error ? <p className={styles.error}>{error}</p> : null}
          <div>
            <label className={styles.fieldLabel} htmlFor="signin-user-id">
              아이디
            </label>
            <input
              id="signin-user-id"
              name="userId"
              type="text"
              autoComplete="username"
              className={styles.fieldInput}
              value={userId}
              onChange={(ev) => setUserId(ev.target.value)}
              placeholder="아이디를 입력하세요"
            />
          </div>
          <div>
            <label className={styles.fieldLabel} htmlFor="signin-password">
              비밀번호
            </label>
            <input
              id="signin-password"
              name="password"
              type="password"
              autoComplete="current-password"
              className={styles.fieldInput}
              value={password}
              onChange={(ev) => setPassword(ev.target.value)}
              placeholder="비밀번호를 입력하세요"
            />
          </div>
          <button type="submit" className={styles.submitBtn} disabled={loading}>
            {loading ? "로그인 중…" : "로그인"}
          </button>
         
        </form>

        <div className={styles.divider} aria-hidden>
          또는
        </div>

        <a href={kakaoOAuthStartUrl} className={styles.kakaoLink}>
          카카오로 로그인 하기
        </a>
      </section>
    </main>
  );
}