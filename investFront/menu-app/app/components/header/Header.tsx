import Link from "next/link";
import styles from "./Header.module.css";

export default function Header() {
  return (
    <header className={styles.header}>
      <div className={styles.inner}>
        {/* 로고 영역 */}
        <Link href="/" className={styles.logoArea}>
          <div className={styles.logoImage} /> 
          {/* 이미지가 생기면 아래 코드로 변경 */}
          {/* <Image
            src="/logo.png"
            alt="KH 증권 로고"
            width={52}
            height={28}
            className={styles.logoImage}
          /> */}
          <span className={styles.logoText}>KH 증권</span>
        </Link>

        {/* 메뉴 영역 */}
        <nav className={styles.nav}>
          <Link href="/" className={styles.navItem}>
            홈
          </Link>
          <Link href="/news" className={styles.navItem}>
            뉴스
          </Link>
          <Link href="/stocks" className={styles.navItem}>
            주식 골라보기
          </Link>
          <Link href="/account" className={styles.navItem}>
            내 계좌
          </Link>
        </nav>

        {/* 로그인 버튼 */}
        <div className={styles.rightArea}>
          <Link href="/login" className={styles.loginButton}>
            로그인
          </Link>
        </div>
      </div>
    </header>
  );
}