"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { apiClient } from "@/lib/api-client";

import layoutStyles from "../myPage.module.css";
import memberStyles from "./member.module.css";
import MyPageSidebar from "../components/MyPageSidebar";

import {
  getCurrentUser,
  clearLoginStorage,
  type LoginUser,
} from "@/lib/auth-user";
import Link from "next/link";

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

export default function MemberPage() {
  const [user, setUser] = useState<LoginUser | null>(null);
  const router = useRouter();

  useEffect(() => {
    async function loadMyInfo() {
      const currentUser = await getCurrentUser();
      setUser(currentUser);
    }

    loadMyInfo();
  }, []);

  async function handleWithdraw() {
  const ok = window.confirm("정말 회원 탈퇴하시겠습니까?");

  if (!ok) return;

  try {
    await apiClient.patch("/users/me/withdraw");

    clearLoginStorage();

    alert("회원 탈퇴가 완료되었습니다.");
    router.replace("/main");
  } catch {
    alert("회원 탈퇴 처리 중 오류가 발생했습니다.");
  }
}

  return (
    <main className={layoutStyles.page}>
      <MyPageSidebar />

      <section className={layoutStyles.content}>
        <div className={layoutStyles.pageTitleArea}>
          <p className={layoutStyles.pageLabel}>MY PAGE</p>
          <h1 className={layoutStyles.pageTitle}>내 정보</h1>
        </div>

        {!user ? (
          <div className={layoutStyles.emptyCard}>
            <h2>로그인이 필요합니다.</h2>
            <p>내 정보를 확인하려면 먼저 로그인해주세요.</p>
          </div>
        ) : (
          <>
            <section className={memberStyles.profileCard}>
              <div className={memberStyles.profileIcon}>
                {user.userName?.slice(0, 1) || "회"}
              </div>

              <div className={memberStyles.profileText}>
                <h2>{user.userName}님</h2>
                <p>
                  {user.provider === "LOCAL"
                    ? `${user.userId || "-"} · ${getProviderName(user.provider)}`
                    : `${getProviderName(user.provider)} 로그인`}
                </p>
              </div>
            </section>

            <section className={memberStyles.infoCard}>
              <div className={memberStyles.cardHeader}>
                <h2>기본 정보</h2>
                <button type="button" className={memberStyles.textButton}>
                  투자 성향 분석
                </button>
              </div>

              <div className={memberStyles.infoList}>
                <div className={memberStyles.infoRow}>
                  <span>이름</span>
                  <strong>{user.userName}</strong>
                </div>

                <div className={memberStyles.infoRow}>
                  <span>{getAccountLabel(user.provider)}</span>
                  <strong>{getAccountValue(user)}</strong>
                </div>

                <div className={memberStyles.infoRow}>
                  <span>이메일</span>
                  <strong>{user.email || "-"}</strong>
                </div>

                <div className={memberStyles.infoRow}>
                  <span>전화번호</span>
                  <strong>{user.phone || "-"}</strong>
                </div>

                <div className={memberStyles.infoRow}>
                  <span>가입 방식</span>
                  <strong>{getProviderName(user.provider)}</strong>
                </div>
              </div>
            </section>

            <section className={memberStyles.manageCard}>
              <div>
                <h2>계정 관리</h2>
                <p>회원정보를 수정하거나 계정을 탈퇴할 수 있습니다.</p>
              </div>

              <div className={memberStyles.actionArea}>
                <Link href="/main/myPage/member/edit">
                  <button type="button" className={memberStyles.primaryButton}>
                    회원정보 수정
                  </button>
                </Link>
                <button
                  type="button"
                  className={memberStyles.dangerButton}
                  onClick={handleWithdraw}
                >
                  회원 탈퇴
                </button>
              </div>
            </section>
          </>
        )}
      </section>
    </main>
  );
}