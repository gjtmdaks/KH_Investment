"use client";

import Image from "next/image";
import Link from "next/link";
import StockSearchBar from "./StockSearchBar";
import { useAuth } from "@/app/context/AuthContext";
import { performLogout } from "@/lib/auth-user";
import styles from "./Header.module.css";

type MainHeaderPayload = {
  userNo?: number;
  userName?: string;
};

export default function Header({ data }: { data?: unknown }) {
  const header = (data as { header?: MainHeaderPayload } | null)?.header;
  const { user, isAuthenticated, isLoading } = useAuth();

  const dataUserName = header?.userName?.trim() ?? "";
  const authUserName = user?.userName?.trim() ?? "";

  const displayName = dataUserName || authUserName || "회원";
  const isLogin = !!dataUserName || isAuthenticated;

  async function handleLogout() {
    await performLogout();
  }

  return (
    <header className={styles.header}>
      <div className={styles.inner}>
        <div className={styles.leftArea}>
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

          <nav className={styles.nav}>
            <Link href="/main" className={styles.navItem}>
              홈
            </Link>
            <Link href="/main/news" className={styles.navItem}>
              뉴스
            </Link>
            <Link href="/main/screener" className={styles.navItem}>
              주식 골라보기
            </Link>
            <Link href="/main/my-account" className={styles.navItem}>
              내 계좌
            </Link>
            <Link href="/main/notice" className={styles.navItem}>
              공지사항
            </Link>
          </nav>
        </div>

        <div className={styles.centerArea}>
          <StockSearchBar />
        </div>

        <div className={styles.rightArea}>
          {!isLoading && isLogin ? (
            <div className={styles.loggedInWrap}>
              <Link href="/main/myPage" className={styles.userLink}>
                <span className={styles.welcome}>
                  {displayName}님, 환영합니다!
                </span>
              </Link>
              <span className={styles.sep}>|</span>
              <button
                type="button"
                className={styles.logoutButton}
                onClick={handleLogout}
              >
                로그아웃
              </button>
            </div>
          ) : !isLoading ? (
            <Link href="/sign-in" className={styles.loginButton}>
              로그인
            </Link>
          ) : null}
        </div>
      </div>
    </header>
  );
}
