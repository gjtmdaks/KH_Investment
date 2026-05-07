import Link from "next/link";
import styles from "./Header.module.css";
import Image from "next/image";

export default function Header({ data }: any) {
  const isLogin = !!data?.header;

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

        {/* 메뉴 영역 */}
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

        {/* 로그인 버튼 */}
        <div className={styles.rightArea}>
          {isLogin ? (
            <span>{data.header.userName}님</span>
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