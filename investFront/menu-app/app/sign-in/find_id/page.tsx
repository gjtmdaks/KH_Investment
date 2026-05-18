"use client";

import { FormEvent, useState } from "react";
import Link from "next/link";
import Image from "next/image";
import { apiClient } from "@/lib/api-client";
import { getApiErrorMessage } from "@/lib/api-error";
import styles from "../find_password/findPassword.module.css";

export default function FindUserIdPage() {
  const [email, setEmail] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);

    if (!email.trim()) {
      setError("이메일을 입력해주세요.");
      return;
    }

    setLoading(true);

    try {
      const { data } = await apiClient.post("/users/find_id", { email: email.trim() });

      if (data.success) {
        alert("가입 이메일로 아이디 정보를 발송했습니다.");
        window.location.href = "/sign-in";
      } else {
        setError(data.message || "아이디 찾기에 실패했습니다.");
      }
    } catch (err: unknown) {
      setError(getApiErrorMessage(err));
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

        <Link href="/sign-in" className={styles.signInLink}>
          로그인
        </Link>
      </div>

      <section className={styles.card}>
        <p className={styles.brand}>KH 증권</p>
        <h1 className={styles.title}>아이디 찾기</h1>
        <p className={styles.description}>
          가입 시 등록한 이메일을 입력하면 해당 이메일로 가입하신 아이디 정보를 보내드립니다.
        </p>

        <form className={styles.form} onSubmit={handleSubmit}>
          {error ? <p className={styles.error}>{error}</p> : null}

          <div>
            <label className={styles.fieldLabel} htmlFor="find-id-email">
              이메일
            </label>
            <input
              id="find-id-email"
              type="email"
              autoComplete="email"
              className={styles.fieldInput}
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="가입 시 등록한 이메일을 입력하세요"
            />
          </div>

          <button type="submit" className={styles.submitBtn} disabled={loading}>
            {loading ? "발송 중..." : "아이디 이메일 발송"}
          </button>
        </form>

        <Link href="/sign-in" className={styles.backLink}>
          로그인 화면으로 돌아가기
        </Link>
      </section>
    </main>
  );
}
