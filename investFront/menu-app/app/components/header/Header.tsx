"use client";

import Image from "next/image";
import Link from "next/link";

import styles from "./Header.module.css";

type MainHeaderPayload = {
  userNo?: number;
  userName?: string;
};

export default function Header({ data }: { data?: unknown }) {
  const header = (data as { header?: MainHeaderPayload } | null)?.header;
  const isLogin = !!(header && header.userName != null && header.userName !== "");

  function handleLogout() {
    try {
      window.localStorage.removeItem("accessToken");
      window.localStorage.removeItem("user");

      window.sessionStorage.clear();
    } catch {
      /* ignore */
    }
    window.location.replace("/main/stock");
  }

  const displayName =
    header?.userName && header.userName.trim() !== ""
      ? header.userName.trim()
      : "회원";

  return (
    <header className={styles.header}>
      <div className={styles.inner}>
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
          <Link href="/main/myAccount" className={styles.navItem}>
            내 계좌
          </Link>
        </nav>

        <div className={styles.rightArea}>
          {isLogin ? (
            <div className={styles.loggedInWrap}>
              <a href="/main/myPage">
              <span className={styles.welcome}>
                {displayName}님, 환영합니다!
              </span>
              </a>
              <span className={styles.sep} aria-hidden>
                |
              </span>
              <button
                type="button"
                className={styles.logoutButton}
                onClick={handleLogout}
              >
                로그아웃
              </button>
            </div>
          ) : (
            <Link href="/sign-in" className={styles.loginButton}>
              로그인
            </Link>
          )}
        </div>
      </div>
    </header>
  );
}
