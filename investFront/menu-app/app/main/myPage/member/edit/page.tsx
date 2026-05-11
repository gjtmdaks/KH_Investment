"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";

import layoutStyles from "../../myPage.module.css";
import memberStyles from "../member.module.css";
import MyPageSidebar from "../../components/MyPageSidebar";

import {
  getCurrentUser,
  type LoginUser,
} from "@/lib/auth-user";
import { apiClient } from "@/lib/api-client";

type ApiEnvelope<T> = {
  success: boolean;
  data: T;
  message?: string | null;
};

type UserUpdateRequest = {
  userName: string;
  email: string | null;
  phone: string | null;
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

  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);

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
  }, []);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);

    if (!userName.trim()) {
      setError("이름을 입력해주세요.");
      return;
    }

    setSaving(true);

    try {
      const requestBody: UserUpdateRequest = {
        userName: userName.trim(),
        email: email.trim() || null,
        phone: phone.trim() || null,
      };

      const { data } = await apiClient.patch<ApiEnvelope<LoginUser>>(
        "/users/me",
        requestBody
      );

      if (!data.success || !data.data) {
        setError(data.message ?? "회원정보 수정에 실패했습니다.");
        return;
      }

      window.localStorage.setItem("user", JSON.stringify(data.data));

      alert("회원정보가 수정되었습니다.");
      router.replace("/main/myPage/member");
    } catch {
      setError("회원정보 수정 중 오류가 발생했습니다.");
    } finally {
      setSaving(false);
    }
  }

  function handleCancel() {
    router.back();
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

              {error ? (
                <p style={{ color: "#ff6b6b", marginBottom: 18 }}>
                  {error}
                </p>
              ) : null}

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
                    style={inputStyle}
                  />
                </div>

                <div className={memberStyles.infoRow}>
                  <span>이메일</span>
                  <input
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="이메일을 입력하세요"
                    style={inputStyle}
                  />
                </div>

                <div className={memberStyles.infoRow}>
                  <span>전화번호</span>
                  <input
                    value={phone}
                    onChange={(e) => setPhone(e.target.value)}
                    placeholder="전화번호를 입력하세요"
                    style={inputStyle}
                  />
                </div>
              </div>
            </section>

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

const inputStyle: React.CSSProperties = {
  width: "100%",
  height: "38px",
  border: "1px solid #2a2d38",
  borderRadius: "10px",
  background: "#111217",
  color: "#ffffff",
  padding: "0 12px",
  fontSize: "14px",
  outline: "none",
  textAlign: "right",
};