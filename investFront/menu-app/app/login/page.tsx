"use client";

import Link from "next/link";
import styles from "./LoginPage.module.css";

const apiBase = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8081";
const kakaoOAuthStartUrl = `${apiBase.replace(/\/$/, "")}/oauth2/authorization/kakao`;

export default function LoginPage() {
  return (
    <main className={styles.container}>
      <section className={styles.card}>
        <p className={styles.brand}>KH 증권</p>
        <h1 className={styles.title}>로그인</h1>
        <p className={styles.description}>
          Spring Security OAuth2
        </p>

        <Link href={kakaoOAuthStartUrl} className={styles.kakaoLink}>
          카카오로 로그인 하기
        </Link>

      </section>
    </main>
  );
}
