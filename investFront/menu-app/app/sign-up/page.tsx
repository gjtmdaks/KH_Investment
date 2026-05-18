"use client";

import Link from "next/link";
import Image from "next/image";
import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { apiClient } from "@/lib/api-client";
import { getApiErrorMessage } from "@/lib/api-error";
import {
  isPasswordPolicyValid,
  PASSWORD_POLICY_HINT,
  validatePassword,
} from "@/lib/password-policy";
import {
  EMAIL_CODE_COOLDOWN_SECONDS,
  formatEmailCooldown,
} from "@/lib/signup-email";
import styles from "./signUp.module.css";

type ApiEnvelope<T> = {
  success: boolean;
  data: T;
  message?: string | null;
};

const EMAIL_PATTERN = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
const PHONE_PATTERN = /^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$/;
const CODE_PATTERN = /^\d{6}$/;

export default function SignUpPage() {
  const router = useRouter();

  const [userId, setUserId] = useState("");
  const [password, setPassword] = useState("");
  const [passwordCheck, setPasswordCheck] = useState("");
  const [userName, setUserName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [emailCode, setEmailCode] = useState("");
  const [emailVerified, setEmailVerified] = useState(false);

  const [error, setError] = useState<string | null>(null);
  const [emailAlert, setEmailAlert] = useState<{
    type: "info" | "error";
    message: string;
  } | null>(null);
  const [loading, setLoading] = useState(false);
  const [sendingCode, setSendingCode] = useState(false);
  const [verifyingCode, setVerifyingCode] = useState(false);
  const [sendCooldown, setSendCooldown] = useState(0);
  const [hasEmailCodeSent, setHasEmailCodeSent] = useState(false);

  const normalizedEmail = email.trim().toLowerCase();
  const normalizedPhone = phone.trim().replace(/\s+/g, "");

  const passwordValidation =
    password.length > 0 ? validatePassword(password) : null;
  const isPasswordInvalid =
    passwordValidation !== null && !passwordValidation.valid;
  const passwordErrorMessage = passwordValidation?.message ?? null;
  const isPasswordMismatch =
    passwordCheck.length > 0 && password !== passwordCheck;
  const isEmailInvalid =
    normalizedEmail.length > 0 && !EMAIL_PATTERN.test(normalizedEmail);
  const isPhoneInvalid =
    normalizedPhone.length > 0 && !PHONE_PATTERN.test(normalizedPhone);
  const isCodeInvalid =
    emailCode.length > 0 && !CODE_PATTERN.test(emailCode.trim());

  useEffect(() => {
    if (sendCooldown <= 0) {
      return;
    }

    const timer = window.setInterval(() => {
      setSendCooldown((prev) => (prev <= 1 ? 0 : prev - 1));
    }, 1000);

    return () => window.clearInterval(timer);
  }, [sendCooldown]);

  useEffect(() => {
    setEmailVerified(false);
    setEmailAlert(null);
    setHasEmailCodeSent(false);
    setSendCooldown(0);
  }, [normalizedEmail]);

  const isFormValid =
    userId.trim() !== "" &&
    password.trim() !== "" &&
    passwordCheck.trim() !== "" &&
    userName.trim() !== "" &&
    normalizedEmail !== "" &&
    normalizedPhone !== "" &&
    isPasswordPolicyValid(password) &&
    password === passwordCheck &&
    EMAIL_PATTERN.test(normalizedEmail) &&
    PHONE_PATTERN.test(normalizedPhone) &&
    emailVerified;

  async function handleSendEmailCode(isResend = false) {
    if (!EMAIL_PATTERN.test(normalizedEmail)) {
      setEmailAlert({
        type: "error",
        message: "올바른 이메일을 입력해주세요.",
      });
      return;
    }

    if (!isResend && sendCooldown > 0) {
      return;
    }

    setEmailAlert(null);
    setSendingCode(true);

    try {
      const { data } = await apiClient.post<ApiEnvelope<null>>(
        "/users/signup/email/send-code",
        { email: normalizedEmail }
      );

      if (!data.success) {
        setEmailAlert({
          type: "error",
          message: data.message ?? "인증번호 발송에 실패했습니다.",
        });
        return;
      }

      setHasEmailCodeSent(true);
      setEmailVerified(false);
      setEmailCode("");
      setEmailAlert({
        type: "info",
        message:
          data.message ??
          (isResend
            ? "인증번호를 다시 발송했습니다. 5분 이내에 입력해주세요."
            : "인증번호를 이메일로 발송했습니다. 5분 이내에 입력해주세요."),
      });
      if (!isResend) {
        setSendCooldown(EMAIL_CODE_COOLDOWN_SECONDS);
      }
    } catch (error: unknown) {
      setEmailAlert({
        type: "error",
        message: getApiErrorMessage(
          error,
          "인증번호 발송 중 서버와 통신할 수 없습니다."
        ),
      });
    } finally {
      setSendingCode(false);
    }
  }

  async function handleVerifyEmailCode() {
    if (!EMAIL_PATTERN.test(normalizedEmail)) {
      setEmailAlert({
        type: "error",
        message: "올바른 이메일을 입력해주세요.",
      });
      return;
    }

    if (!CODE_PATTERN.test(emailCode.trim())) {
      setEmailAlert({
        type: "error",
        message: "인증번호 6자리를 입력해주세요.",
      });
      return;
    }

    setVerifyingCode(true);

    try {
      const { data } = await apiClient.post<ApiEnvelope<null>>(
        "/users/signup/email/verify-code",
        { email: normalizedEmail, code: emailCode.trim() }
      );

      if (!data.success) {
        setEmailAlert({
          type: "error",
          message: data.message ?? "이메일 인증에 실패했습니다.",
        });
        setEmailVerified(false);
        return;
      }

      setEmailVerified(true);
      setEmailAlert({
        type: "info",
        message: data.message ?? "이메일 인증이 완료되었습니다.",
      });
    } catch (error: unknown) {
      setEmailAlert({
        type: "error",
        message: getApiErrorMessage(
          error,
          "이메일 인증 중 서버와 통신할 수 없습니다."
        ),
      });
      setEmailVerified(false);
    } finally {
      setVerifyingCode(false);
    }
  }

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
          passwordConfirm: passwordCheck,
          userName,
          email: normalizedEmail,
          phone: normalizedPhone,
        }
      );

      if (!data.success) {
        setError(data.message ?? "회원가입에 실패했습니다.");
        return;
      }

      alert("회원가입이 완료되었습니다.");
      router.push("/sign-in");
    } catch (error: unknown) {
      setError(getApiErrorMessage(error));
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
              className={`${styles.fieldInput} ${
                isPasswordInvalid ? styles.inputError : ""
              }`}
              value={password}
              onChange={(ev) => setPassword(ev.target.value)}
              placeholder="비밀번호를 입력하세요"
            />
            <p className={styles.fieldHint}>{PASSWORD_POLICY_HINT}</p>
            {isPasswordInvalid ? (
              <p className={styles.fieldError}>{passwordErrorMessage}</p>
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

          <div>
            <label className={styles.fieldLabel} htmlFor="signup-phone">
              전화번호
            </label>
            <input
              id="signup-phone"
              name="phone"
              type="tel"
              autoComplete="tel"
              className={`${styles.fieldInput} ${
                isPhoneInvalid ? styles.inputError : ""
              }`}
              value={phone}
              onChange={(ev) => setPhone(ev.target.value)}
              placeholder="01012345678"
            />
            {isPhoneInvalid ? (
              <p className={styles.fieldError}>
                올바른 휴대전화 번호를 입력해주세요.
              </p>
            ) : null}
          </div>

          <div>
            <label className={styles.fieldLabel} htmlFor="signup-email">
              이메일
            </label>
            {emailAlert ? (
              <p
                className={
                  emailAlert.type === "error"
                    ? styles.emailAlertError
                    : styles.emailAlertInfo
                }
              >
                {emailAlert.message}
              </p>
            ) : null}
            <div className={styles.inlineRow}>
              <input
                id="signup-email"
                name="email"
                type="email"
                autoComplete="email"
                className={`${styles.fieldInput} ${
                  isEmailInvalid ? styles.inputError : ""
                }`}
                value={email}
                onChange={(ev) => setEmail(ev.target.value)}
                placeholder="example@email.com"
              />
              <button
                type="button"
                className={styles.secondaryBtn}
                onClick={() => handleSendEmailCode(false)}
                disabled={
                  sendingCode ||
                  sendCooldown > 0 ||
                  !EMAIL_PATTERN.test(normalizedEmail) ||
                  emailVerified
                }
              >
                {sendingCode
                  ? "발송 중…"
                  : sendCooldown > 0
                    ? formatEmailCooldown(sendCooldown)
                    : "인증번호 발송"}
              </button>
            </div>
            {hasEmailCodeSent && !emailVerified ? (
              <div className={styles.resendRow}>
                <button
                  type="button"
                  className={styles.resendBtn}
                  onClick={() => handleSendEmailCode(true)}
                  disabled={
                    sendingCode || !EMAIL_PATTERN.test(normalizedEmail)
                  }
                >
                  새 인증
                </button>
              </div>
            ) : null}
            {isEmailInvalid ? (
              <p className={styles.fieldError}>올바른 이메일을 입력해주세요.</p>
            ) : null}
            {emailVerified ? (
              <p className={styles.fieldSuccess}>이메일 인증 완료</p>
            ) : null}
          </div>

          <div>
            <label className={styles.fieldLabel} htmlFor="signup-email-code">
              이메일 인증번호 (6자리)
            </label>
            <div className={styles.inlineRow}>
              <input
                id="signup-email-code"
                name="emailCode"
                type="text"
                inputMode="numeric"
                maxLength={6}
                className={`${styles.fieldInput} ${
                  isCodeInvalid ? styles.inputError : ""
                }`}
                value={emailCode}
                onChange={(ev) =>
                  setEmailCode(ev.target.value.replace(/\D/g, "").slice(0, 6))
                }
                placeholder="123456"
              />
              <button
                type="button"
                className={styles.secondaryBtn}
                onClick={handleVerifyEmailCode}
                disabled={verifyingCode || emailVerified}
              >
                {emailVerified
                  ? "인증 완료"
                  : verifyingCode
                    ? "확인 중…"
                    : "인증 확인"}
              </button>
            </div>
            {isCodeInvalid ? (
              <p className={styles.fieldError}>인증번호는 6자리 숫자입니다.</p>
            ) : null}
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
