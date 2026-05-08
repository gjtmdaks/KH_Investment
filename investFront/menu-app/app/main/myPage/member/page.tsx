"use client";

import { useEffect, useState } from "react";
import layoutStyles from "../myPage.module.css";
import memberStyles from "./member.module.css";
import MyPageSidebar from "../components/MyPageSidebar";

type LoginUser = {
  accessToken?: string;
  userNo: number;
  userId: string;
  userName: string;
  email?: string | null;
  phone?: string | null;
  provider: string;
  auth: number;
};

export default function MemberPage() {
  const [user, setUser] = useState<LoginUser | null>(null);

  useEffect(() => {
    const savedUser = window.localStorage.getItem("user");

    if (!savedUser) {
      setUser(null);
      return;
    }

    try {
      setUser(JSON.parse(savedUser));
    } catch {
      setUser(null);
    }
  }, []);

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
                  {user.userId} · {user.provider}
                </p>
              </div>
            </section>

            <section className={memberStyles.infoCard}>
              <div className={memberStyles.cardHeader}>
                <h2>기본 정보</h2>
                <button type="button" className={memberStyles.textButton}>
                  정보 수정
                </button>
              </div>

              <div className={memberStyles.infoList}>
                <div className={memberStyles.infoRow}>
                  <span>이름</span>
                  <strong>{user.userName}</strong>
                </div>

                <div className={memberStyles.infoRow}>
                  <span>아이디</span>
                  <strong>{user.userId}</strong>
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
                  <strong>{user.provider}</strong>
                </div>
              </div>
            </section>

            <section className={memberStyles.manageCard}>
              <div>
                <h2>계정 관리</h2>
                <p>회원정보를 수정하거나 계정을 탈퇴할 수 있습니다.</p>
              </div>

              <div className={memberStyles.actionArea}>
                <button type="button" className={memberStyles.primaryButton}>
                  회원정보 수정
                </button>
                <button type="button" className={memberStyles.dangerButton}>
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