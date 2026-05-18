"use client";

import { FormEvent, useState } from "react";
import Link from "next/link";
import Image from "next/image";
import { apiClient } from "@/lib/api-client";
import {
  isPasswordPolicyValid,
  PASSWORD_POLICY_HINT,
  validatePassword,
} from "@/lib/password-policy";
import styles from "./findPassword.module.css";

export default function ResetPasswordPage() {
  const [userId, setUserId] = useState("");
  const [userName, setUserName] = useState("");
  const [newPassword, setNewPassword] = useState("");

  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const passwordValidation =
    newPassword.length > 0 ? validatePassword(newPassword) : null;
  const isPasswordInvalid =
    passwordValidation !== null && !passwordValidation.valid;

  async function handleReset(e: FormEvent) {
    e.preventDefault();
    setError(null);

    if (!userId.trim()) {
      setError("아이디를 입력해주세요.");
      return;
    }

    if (!userName.trim()) {
      setError("이름을 입력해주세요.");
      return;
    }

    const passwordCheck = validatePassword(newPassword);
    if (!passwordCheck.valid) {
      setError(passwordCheck.message ?? "비밀번호 형식이 올바르지 않습니다.");
      return;
    }

    setLoading(true);

    try {
      const { data } = await apiClient.post("/users/find_password", {
        userId,
        userName,
        newPassword,
      });

      if (data.success) {
        alert("비밀번호가 변경되었습니다.");
        window.location.href = "/sign-in";
      } else {
        setError(data.message || "비밀번호 변경에 실패했습니다.");
      }
    } catch (error: any) {
      setError(error.response?.data?.message || "서버와 통신할 수 없습니다.");
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
        <h1 className={styles.title}>비밀번호 찾기</h1>
        <p className={styles.description}>
          가입한 아이디와 이름을 입력한 뒤 새 비밀번호로 변경하세요.
        </p>

        <form className={styles.form} onSubmit={handleReset}>
          {error ? <p className={styles.error}>{error}</p> : null}

          <div>
            <label className={styles.fieldLabel} htmlFor="user-id">
              아이디
            </label>
            <input
              id="user-id"
              type="text"
              className={styles.fieldInput}
              value={userId}
              onChange={(e) => setUserId(e.target.value)}
              placeholder="아이디를 입력하세요"
            />
          </div>

          <div>
            <label className={styles.fieldLabel} htmlFor="user-name">
              이름
            </label>
            <input
              id="user-name"
              type="text"
              className={styles.fieldInput}
              value={userName}
              onChange={(e) => setUserName(e.target.value)}
              placeholder="이름을 입력하세요"
            />
          </div>

          <div>
            <label className={styles.fieldLabel} htmlFor="new-password">
              새 비밀번호
            </label>
            <input
              id="new-password"
              type="password"
              className={`${styles.fieldInput} ${
                isPasswordInvalid ? styles.inputError : ""
              }`}
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              placeholder="새 비밀번호를 입력하세요"
            />
            <p className={styles.fieldHint}>{PASSWORD_POLICY_HINT}</p>
            {isPasswordInvalid ? (
              <p className={styles.fieldError}>
                {passwordValidation?.message}
              </p>
            ) : null}
          </div>

          <button
            type="submit"
            className={styles.submitBtn}
            disabled={loading || !isPasswordPolicyValid(newPassword)}
          >
            {loading ? "변경 중..." : "비밀번호 변경"}
          </button>
        </form>

        <Link href="/sign-in" className={styles.backLink}>
          로그인 화면으로 돌아가기
        </Link>
      </section>
    </main>
  );
}