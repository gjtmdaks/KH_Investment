"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";

import layoutStyles from "../../../myPage.module.css";
import memberStyles from "../../member.module.css";
import verifyStyles from "./verify.module.css";
import MyPageSidebar from "../../../components/MyPageSidebar";

import { getCurrentUser, type LoginUser } from "@/lib/auth-user";
import { apiClient } from "@/lib/api-client";
import { getApiErrorMessage } from "@/lib/api-error";
import { saveMemberEditAuth } from "@/lib/member-edit-auth";

type ApiEnvelope<T> = {
  success: boolean;
  data: T;
  message?: string | null;
};

type VerifyPasswordResponse = {
  editToken: string;
};

export default function MemberEditVerifyPage() {
  const router = useRouter();

  const [user, setUser] = useState<LoginUser | null>(null);
  const [currentPassword, setCurrentPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    async function loadUser() {
      setLoading(true);
      setError(null);

      try {
        const currentUser = await getCurrentUser();
        setUser(currentUser);

        if (currentUser && currentUser.provider !== "LOCAL") {
          router.replace("/main/myPage/member/edit");
        }
      } catch {
        setError("회원 정보를 불러오지 못했습니다.");
      } finally {
        setLoading(false);
      }
    }

    loadUser();
  }, [router]);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);

    if (!currentPassword.trim()) {
      setError("현재 비밀번호를 입력해주세요.");
      return;
    }

    setSubmitting(true);

    try {
      const { data } = await apiClient.post<ApiEnvelope<VerifyPasswordResponse>>(
        "/users/me/password/verify",
        { currentPassword }
      );

      if (!data.success || !data.data?.editToken) {
        setError(data.message ?? "비밀번호 확인에 실패했습니다.");
        return;
      }

      if (!user) {
        setError("로그인이 필요합니다.");
        return;
      }

      saveMemberEditAuth(user.userNo, data.data.editToken);
      router.replace("/main/myPage/member/edit");
    } catch (err: unknown) {
      setError(getApiErrorMessage(err, "비밀번호 확인 중 오류가 발생했습니다."));
    } finally {
      setSubmitting(false);
    }
  }

  function handleCancel() {
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
            <section className={verifyStyles.formCard}>
              <div className={memberStyles.cardHeader}>
                <h2>비밀번호 확인</h2>
              </div>
              <p className={verifyStyles.description}>
                회원정보를 수정하려면 현재 비밀번호를 입력해주세요.
              </p>

              {error ? <p className={verifyStyles.error}>{error}</p> : null}

              <div className={verifyStyles.field}>
                <label htmlFor="current-password">현재 비밀번호</label>
                <input
                  id="current-password"
                  name="currentPassword"
                  type="password"
                  autoComplete="current-password"
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  placeholder="현재 비밀번호를 입력하세요"
                />
              </div>
            </section>

            <section className={memberStyles.manageCard}>
              <div>
                <h2>본인 확인</h2>
                <p>확인 후 회원정보 수정 화면으로 이동합니다.</p>
              </div>

              <div className={memberStyles.actionArea}>
                <button
                  type="button"
                  className={memberStyles.dangerButton}
                  onClick={handleCancel}
                  disabled={submitting}
                >
                  취소
                </button>
                <button
                  type="submit"
                  className={memberStyles.primaryButton}
                  disabled={submitting}
                >
                  {submitting ? "확인 중..." : "확인"}
                </button>
              </div>
            </section>
          </form>
        )}
      </section>
    </main>
  );
}
