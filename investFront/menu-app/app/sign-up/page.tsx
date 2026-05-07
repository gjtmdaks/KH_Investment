"use client";

import Link from "next/link";
import Image from "next/image";
import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { apiClient } from "@/lib/api-client";
import styles from "./signUp.module.css";

type ApiEnvelope<T> = {
  success: boolean;
  data: T;
  message?: string | null;
};

export default function SignUpPage() {
  const router = useRouter();

  const [userId, setUserId] = useState("");
  const [password, setPassword] = useState("");
  const [passwordCheck, setPasswordCheck] = useState("");
  const [userName, setUserName] = useState("");

  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const isPasswordTooShort = password.length > 0 && password.length < 4;

  const isPasswordMismatch =
    passwordCheck.length > 0 && password !== passwordCheck;

  // 비밀번호 아이디 체크
  const isFormValid =
    userId.trim() !== "" &&
    password.trim() !== "" &&
    passwordCheck.trim() !== "" &&
    userName.trim() !== "" &&
    password.length >= 4 &&  // 비밀번호 최소
    password === passwordCheck;

  async function handleSignUp(e: FormEvent) {
    e.preventDefault();

    if (!isFormValid) {
      setError("입력값을 다시 확인해주세요.");
      return;
    }

    setError(null);
    setLoading(true);

    try {
      const { data } = await apiClient.post<ApiEnvelope<null>>(
        "/users/signup",
        {
          userId,
          password,
          userName,
        }
      );

      if (!data.success) {
        setError(data.message ?? "회원가입에 실패했습니다.");
        return;
      }

      alert("회원가입이 완료되었습니다.");
      router.push("/sign-in");
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
      </div>

      <section className={styles.card}>
        <p className={styles.brand}>KH 증권</p>
        <h1 className={styles.title}>회원가입</h1>
        <p className={styles.description}>
          로컬 계정으로 KH 증권 서비스를 시작해보세요.
        </p>

        <form className={styles.form} onSubmit={handleSignUp}>
          {error ? <p className={styles.error}>{error}</p> : null}

          <div>
            <label className={styles.fieldLabel} htmlFor="signup-user-id">
              아이디
            </label>
            <input
              id="signup-user-id"
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
            <label className={styles.fieldLabel} htmlFor="signup-password">
              비밀번호
            </label>
            <input
              id="signup-password"
              name="password"
              type="password"
              autoComplete="new-password"
              className={styles.fieldInput}
              value={password}
              onChange={(ev) => setPassword(ev.target.value)}
              placeholder="비밀번호를 입력하세요"
            />
            {isPasswordTooShort ? (
              <p className={styles.fieldError}>비밀번호는 최소 4글자 이상이어야 합니다.</p>
            ) : null}
          </div>

          <div>
            <label
              className={styles.fieldLabel}
              htmlFor="signup-password-check"
            >
              비밀번호 확인
            </label>
            <input
              id="signup-password-check"
              name="passwordCheck"
              type="password"
              autoComplete="new-password"
              className={`${styles.fieldInput} ${
                isPasswordMismatch ? styles.inputError : ""
              }`}
              value={passwordCheck}
              onChange={(ev) => setPasswordCheck(ev.target.value)}
              placeholder="비밀번호를 한 번 더 입력하세요"
            />

            {isPasswordMismatch ? (
              <p className={styles.fieldError}>비밀번호가 일치하지 않습니다.</p>
            ) : null}
          </div>

          <div>
            <label className={styles.fieldLabel} htmlFor="signup-user-name">
              이름
            </label>
            <input
              id="signup-user-name"
              name="userName"
              type="text"
              autoComplete="name"
              className={styles.fieldInput}
              value={userName}
              onChange={(ev) => setUserName(ev.target.value)}
              placeholder="이름을 입력하세요"
            />
          </div>

          <button
            type="submit"
            className={styles.submitBtn}
            disabled={!isFormValid || loading}
          >
            {loading ? "가입 중…" : "회원가입"}
          </button>
        </form>

        <div className={styles.bottomArea}>
          <span>이미 계정이 있나요?</span>
          <Link href="/sign-in">로그인</Link>
        </div>
      </section>
    </main>
  );
}