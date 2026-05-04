import styles from "./Header.module.css";

export default function Header() {
  return (
    <header className={styles.header}>
      <div className={styles.container}>
        {/* 왼쪽: 로고 및 서비스명 */}
        <div className={styles.logoArea}>
          <div className={styles.logoIcon}></div>
          <h1 className={styles.title}>KH 증권</h1>
        </div>

        {/* 중앙: 네비게이션 메뉴 */}
        <nav className={styles.nav}>
          <ul className={styles.navList}>
            <li><a href="#home">홈</a></li>
            <li><a href="#news">뉴스</a></li>
            <li><a href="#stocks">주식 골라보기</a></li>
            <li><a href="#account">내 계좌</a></li>
          </ul>
        </nav>

        {/* 오른쪽: 로그인 버튼 */}
        <div className={styles.authArea}>
          <button className={styles.loginBtn}>
            <span className={styles.userIcon}>👤</span> 로그인
          </button>
        </div>
      </div>
    </header>
  );
}