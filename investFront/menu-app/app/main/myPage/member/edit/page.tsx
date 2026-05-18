"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";

import layoutStyles from "../../myPage.module.css";
import memberStyles from "../member.module.css";
import editStyles from "./edit.module.css";
import MyPageSidebar from "../../components/MyPageSidebar";

import { getCurrentUser, type LoginUser } from "@/lib/auth-user";
import { apiClient } from "@/lib/api-client";
import { getApiErrorMessage } from "@/lib/api-error";
import {
  clearMemberEditAuth,
  getMemberEditToken,
  hasValidMemberEditAuth,
} from "@/lib/member-edit-auth";
import {
  isPasswordPolicyValid,
  PASSWORD_POLICY_HINT,
  validatePassword,
} from "@/lib/password-policy";

type ApiEnvelope<T> = {
  success: boolean;
  data: T;
  message?: string | null;
};

type UserUpdateRequest = {
  editToken?: string;
  userName: string;
  email: string | null;
  phone: string | null;
  newPassword?: string;
  newPasswordConfirm?: string;
};

function getProviderName(provider?: string) {
  switch (provider) {
    case "LOCAL":
      return "로컬";
    case "KAKAO":
      return "카카오";
    case "NAVER":
      return "네이버";
    default:
      return provider || "-";
  }
}

function getAccountLabel(provider?: string) {
  return provider === "LOCAL" ? "아이디" : "연동 계정";
}

function getAccountValue(user: LoginUser) {
  if (user.provider === "LOCAL") {
    return user.userId || "-";
  }

  return `${getProviderName(user.provider)} 로그인`;
}

export default function MemberEditPage() {
  const router = useRouter();

  const [user, setUser] = useState<LoginUser | null>(null);
  const [userName, setUserName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [newPasswordConfirm, setNewPasswordConfirm] = useState("");

  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  const isLocalUser = user?.provider === "LOCAL";
  const passwordValidation =
    newPassword.length > 0 ? validatePassword(newPassword) : null;
  const isNewPasswordInvalid =
    passwordValidation !== null && !passwordValidation.valid;
  const newPasswordErrorMessage = passwordValidation?.message ?? null;
  const isNewPasswordMismatch =
    newPasswordConfirm.length > 0 && newPassword !== newPasswordConfirm;
  const isChangingPassword =
    newPassword.length > 0 || newPasswordConfirm.length > 0;

  useEffect(() => {
    async function loadMyInfo() {
      setLoading(true);
      setError(null);

      try {
        const currentUser = await getCurrentUser();

        if (!currentUser) {
          setUser(null);
          return;
        }

        if (
          currentUser.provider === "LOCAL" &&
          !hasValidMemberEditAuth(currentUser.userNo)
        ) {
          router.replace("/main/myPage/member/edit/verify");
          return;
        }

        setUser(currentUser);
        setUserName(currentUser.userName || "");
        setEmail(currentUser.email || "");
        setPhone(currentUser.phone || "");
      } catch {
        setError("회원 정보를 불러오지 못했습니다.");
      } finally {
        setLoading(false);
      }
    }

    loadMyInfo();
  }, [router]);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);

    if (!userName.trim()) {
      setError("이름을 입력해주세요.");
      return;
    }

    if (isChangingPassword) {
      if (!newPassword.trim() || !newPasswordConfirm.trim()) {
        setError("새 비밀번호와 비밀번호 확인을 모두 입력해주세요.");
        return;
      }

      if (!isPasswordPolicyValid(newPassword)) {
        setError(
          validatePassword(newPassword).message ??
            "비밀번호 규칙을 확인해주세요."
        );
        return;
      }

      if (newPassword !== newPasswordConfirm) {
        setError("새 비밀번호가 일치하지 않습니다.");
        return;
      }
    }

    const editToken = user ? getMemberEditToken(user.userNo) : null;

    if (isLocalUser && !editToken) {
      router.replace("/main/myPage/member/edit/verify");
      return;
    }

    setSaving(true);

    try {
      const requestBody: UserUpdateRequest = {
        userName: userName.trim(),
        email: email.trim() || null,
        phone: phone.trim() || null,
      };

      if (isLocalUser && editToken) {
        requestBody.editToken = editToken;
      }

      if (isChangingPassword) {
        requestBody.newPassword = newPassword;
        requestBody.newPasswordConfirm = newPasswordConfirm;
      }

      const { data } = await apiClient.patch<ApiEnvelope<LoginUser>>(
        "/users/me",
        requestBody
      );

      if (!data.success || !data.data) {
        setError(data.message ?? "회원정보 수정에 실패했습니다.");
        return;
      }

      clearMemberEditAuth();
      window.localStorage.setItem("user", JSON.stringify(data.data));

      alert("회원정보가 수정되었습니다.");
      router.replace("/main/myPage/member");
    } catch (err: unknown) {
      setError(getApiErrorMessage(err, "회원정보 수정 중 오류가 발생했습니다."));
    } finally {
      setSaving(false);
    }
  }

  function handleCancel() {
    clearMemberEditAuth();
    router.replace("/main/myPage/member");
  }

  return (
    <main className={layoutStyles.page}>
      <MyPageSidebar />

      <section className={layoutStyles.content}>
        <div className={layoutStyles.pageTitleArea}>
          <p className={layoutStyles.pageLabel}>MY PAGE</p>
          <h1 className={layoutStyles.pageTitle}>회원정보 수정</h1>
        </div>

        {loading ? (
          <div className={layoutStyles.emptyCard}>
            <h2>회원 정보를 불러오는 중입니다.</h2>
            <p>잠시만 기다려주세요.</p>
          </div>
        ) : !user ? (
          <div className={layoutStyles.emptyCard}>
            <h2>로그인이 필요합니다.</h2>
            <p>회원정보를 수정하려면 먼저 로그인해주세요.</p>
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            <section className={memberStyles.infoCard}>
              <div className={memberStyles.cardHeader}>
                <h2>수정 정보</h2>
              </div>

              {error ? <p className={editStyles.error}>{error}</p> : null}

              <div className={memberStyles.infoList}>
                <div className={memberStyles.infoRow}>
                  <span>회원번호</span>
                  <strong>{user.userNo}</strong>
                </div>

                <div className={memberStyles.infoRow}>
                  <span>{getAccountLabel(user.provider)}</span>
                  <strong>{getAccountValue(user)}</strong>
                </div>

                <div className={memberStyles.infoRow}>
                  <span>가입 방식</span>
                  <strong>{getProviderName(user.provider)}</strong>
                </div>

                <div className={memberStyles.infoRow}>
                  <span>이름</span>
                  <input
                    value={userName}
                    onChange={(e) => setUserName(e.target.value)}
                    placeholder="이름을 입력하세요"
                    className={editStyles.input}
                  />
                </div>

                <div className={memberStyles.infoRow}>
                  <span>이메일</span>
                  <input
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="이메일을 입력하세요"
                    className={editStyles.input}
                  />
                </div>

                <div className={memberStyles.infoRow}>
                  <span>전화번호</span>
                  <input
                    value={phone}
                    onChange={(e) => setPhone(e.target.value)}
                    placeholder="전화번호를 입력하세요"
                    className={editStyles.input}
                  />
                </div>
              </div>
            </section>

            {isLocalUser ? (
              <section className={memberStyles.infoCard}>
                <div className={memberStyles.cardHeader}>
                  <h2>비밀번호 변경</h2>
                </div>
                <p className={editStyles.hint}>
                  변경하지 않으려면 비워두세요. {PASSWORD_POLICY_HINT}
                </p>

                <div className={memberStyles.infoList}>
                  <div className={memberStyles.infoRow}>
                    <span>새 비밀번호</span>
                    <div className={editStyles.inputWrap}>
                      <input
                        type="password"
                        autoComplete="new-password"
                        value={newPassword}
                        onChange={(e) => setNewPassword(e.target.value)}
                        placeholder="새 비밀번호"
                        className={`${editStyles.input} ${
                          isNewPasswordInvalid ? editStyles.inputError : ""
                        }`}
                      />
                      {isNewPasswordInvalid ? (
                        <p className={editStyles.fieldError}>
                          {newPasswordErrorMessage}
                        </p>
                      ) : null}
                    </div>
                  </div>

                  <div className={memberStyles.infoRow}>
                    <span>새 비밀번호 확인</span>
                    <div className={editStyles.inputWrap}>
                      <input
                        type="password"
                        autoComplete="new-password"
                        value={newPasswordConfirm}
                        onChange={(e) => setNewPasswordConfirm(e.target.value)}
                        placeholder="새 비밀번호 확인"
                        className={`${editStyles.input} ${
                          isNewPasswordMismatch ? editStyles.inputError : ""
                        }`}
                      />
                      {isNewPasswordMismatch ? (
                        <p className={editStyles.fieldError}>
                          새 비밀번호가 일치하지 않습니다.
                        </p>
                      ) : null}
                    </div>
                  </div>
                </div>
              </section>
            ) : null}

            <section className={memberStyles.manageCard}>
              <div>
                <h2>저장하기</h2>
              </div>

              <div className={memberStyles.actionArea}>
                <button
                  type="button"
                  className={memberStyles.dangerButton}
                  onClick={handleCancel}
                  disabled={saving}
                >
                  취소
                </button>

                <button
                  type="submit"
                  className={memberStyles.primaryButton}
                  disabled={saving}
                >
                  {saving ? "저장 중..." : "저장"}
                </button>
              </div>
            </section>
          </form>
        )}
      </section>
    </main>
  );
}
